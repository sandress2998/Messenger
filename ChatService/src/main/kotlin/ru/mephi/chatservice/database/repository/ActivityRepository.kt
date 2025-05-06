package ru.mephi.chatservice.database.repository

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.property.SecurityProperties
import java.time.Duration
import java.util.*


@Repository
class ActivityRepository (
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val chatMembersRepository: ChatMembersRepository,
    private val securityProperties: SecurityProperties
) {
    val redisOpsForList = redisTemplate.opsForList()
    val timeToLiveInMinutes: Duration = Duration.ofMinutes(securityProperties.jwtTimeoutInMinutes)

    fun addToChat(userId: UUID, chatId: UUID): Mono<Boolean> {
        val key = "chat_activity:$chatId"
        return redisOpsForList.range(key, 0, -1)
            .collectList()
            .flatMap { activeUsersInChat ->
                val isAdded: Boolean
                if (!activeUsersInChat.contains(userId.toString())) {
                    isAdded = true
                    redisOpsForList.rightPush("chat_activity:$chatId", userId.toString())
                } else {
                    isAdded = false
                    Mono.empty()
                }
                .then(updateTTL(key))
                .thenReturn(isAdded)
            }

    }

    fun deleteFromChat(userId: UUID, chatId: UUID): Mono<Void> {
        val key = "chat_activity:$chatId"
        return redisOpsForList.range(key, 0, -1)
            .collectList()
            .flatMap { activeUsersInChat ->
                if (activeUsersInChat.size == 1) {
                    redisOpsForList.delete(key)
                } else {
                    redisOpsForList.remove(key, 1, userId.toString())
                }
            }
            .then()
    }

    fun getActiveChatMembers(chatId: UUID): Flux<UUID> {
        val key = "chat_activity:$chatId"
        return redisOpsForList.range(key, 0, -1)
            .map { activeMember -> UUID.fromString(activeMember)  }
    }

    fun isMemberActive(userId: UUID, chatId: UUID): Mono<Boolean> {
        val key = "chat_activity:$chatId"
        return redisOpsForList.indexOf(key, userId.toString())
            .map { index ->
                index.toInt() != -1
            }
    }

    fun deleteChat(chatId: UUID): Mono<Void> {
        val key = "chat_activity:$chatId"
        return redisOpsForList.delete(key)
            .then()
    }

    private fun updateTTL(key: String): Mono<Void> {
        return redisTemplate.expire(key, timeToLiveInMinutes)
            .then()
    }
}

/*
@Repository
class ActivityRepository (
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val messageKafkaTemplate: KafkaTemplate<String, ChatActivityChangeBroadcast>,
    private val securityProperties: SecurityProperties
) {
    val redisOpsForList = redisTemplate.opsForList()
    val timeToLiveInMinutes: Duration = Duration.ofMinutes(securityProperties.jwtTimeoutInMinutes)

    fun connectToChat(email: String, chatID: String): Mono<Void> {
        return addChatToChatList(email, chatID) // Добавляем чат в список пользователя
            .then(checkIfUserIsInChat(email, chatID)) // Проверяем, есть ли пользователь в чате
            .flatMap { isContained ->
                if (!isContained) {
                    println("Trying to add user $email to chat $chatID")
                    addUserToChat(email, chatID) // Добавляем пользователя в чат
                        .then(notifyChatMembers(email, chatID, "active")) // Уведомляем участников чата
                } else {
                    Mono.empty() // Если пользователь уже в чате, ничего не делаем
                }
            }
            .then(updateTTL("chat_members:$chatID")) // Обновляем TTL для списка участников чата
            .then() // Завершаем поток
    }

    fun disconnectFromChat(email: String, chatID: String): Mono<Void> {
        return redisOpsForList.range("user_chats:$email", 0, -1)
            .collectList()
            .map { list -> list.count { it == chatID } }
            .flatMap { countOccurrences ->
                if (countOccurrences == 1) {
                    removeChatFromChatList(email, chatID)
                        .then(removeUserFromChat(email, chatID))
                } else if (countOccurrences > 1) {
                    removeChatFromChatList(email, chatID)
                } else {
                    Mono.empty()
                }
            }
    }

    fun fetchUsersExceptOne(emailToExcept: String, chatID: String): Mono<List<String>> {
        return redisOpsForList.range("chat_members:$chatID", 0, -1)
            .collectList()
            .map { list -> list.filter { email -> email != emailToExcept } }
    }

    // Проверяем, есть ли пользователь в чате
    private fun checkIfUserIsInChat(email: String, chatID: String): Mono<Boolean> {
        return redisOpsForList.range("chat_members:$chatID", 0, -1)
            .collectList()
            .map { list -> list.contains(email) }
    }

    // Добавляем пользователя в чат
    private fun addUserToChat(email: String, chatID: String): Mono<Long> {
        return redisOpsForList.rightPush("chat_members:$chatID", email)
    }

    // Уведомляем участников чата об изменении статуса пользователя
    private fun notifyChatMembers(email: String, chatID: String, status: String): Mono<Void> {
        return fetchUsersExceptOne(email, chatID) // Получаем список пользователей для уведомления
            .flatMapMany { Flux.fromIterable(it) } // Преобразуем List в Flux
            .flatMap { userToNotify ->
                val messageToNotify = ChatActivityChangeBroadcast(
                    chatID, email, status, userToNotify
                )
                println("Trying to notify $userToNotify about activity change")
                Mono.fromFuture(messageKafkaTemplate.send("activity-from-presence-to-ws", messageToNotify))
            }
            .then() // Завершаем поток уведомлений
    }

    private fun addChatToChatList(email: String, chatID: String): Mono<Void> {
        return redisOpsForList.rightPush("user_chats:$email", chatID)
            .then()
    }

    private fun removeChatFromChatList(email: String, chatID: String): Mono<Void> {
        return redisOpsForList.remove("user_chats:$email", 1, chatID)
            .flatMap {
                redisOpsForList.size("user_chats:$email")
            }
            .flatMap { size ->
                if (size?.toInt() == 0) {
                    redisTemplate.delete("user_chats:$email")
                } else {
                    Mono.empty()
                }
            }
            .then()
    }

    private fun removeUserFromChat(email: String, chatID: String): Mono<Void> {
        return redisOpsForList.remove("chat_members:$chatID", 1, email) // Удаляем пользователя из чата
            .flatMap { removedCount ->
                if (removedCount > 0) {
                    notifyChatMembers(email, chatID, "inactive") // Уведомляем участников чата
                        .then(checkAndDeleteChatIfEmpty(chatID)) // Проверяем, пуст ли чат, и удаляем ключ, если нужно
                } else {
                    Mono.empty() // Если пользователь не был удален, ничего не делаем
                }
            }
    }

    // Проверяем, пуст ли чат, и удаляем ключ, если нужно
    private fun checkAndDeleteChatIfEmpty(chatID: String): Mono<Void> {
        return redisOpsForList.size("chat_members:$chatID")
            .flatMap { size ->
                if (size.toInt() == 0) {
                    redisTemplate.delete("chat_members:$chatID") // Удаляем ключ, если список пуст
                        .then()
                } else {
                    Mono.empty() // Если список не пуст, ничего не делаем
                }
            }
    }

    private fun updateTTL(key: String): Mono<Boolean> {
        return redisTemplate.expire(key, timeToLiveInMinutes)
    }
}
/*
 */