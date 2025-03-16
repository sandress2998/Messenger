package ru.mephi.presence.database

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.ChatActivityChangeBroadcast
import ru.mephi.presence.property.SecurityProperties
import java.time.Duration


@Repository
class StatusRepository(
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
fun setActiveStatus(email: String): Mono<Void> {
    return redisOpsForValue.get("user_status:$email")
        .flatMap {
            incrementSessionQuantity(email)
                .then(updateTTL("user_chats:$email"))
        }
        .switchIfEmpty(
            createActiveStatus(email)
        )
        .then()
    }

fun setInactiveStatus(email: String): Mono<Void> {
    return redisOpsForValue.get("user_status:$email")
        .flatMap { sessionsQuantity ->
            if (sessionsQuantity.toInt() == 1) {
                val deleteUserStatus = redisOpsForValue.delete("user_status:$email")
                val deleteUserInChats = redisOpsForList.range("user_chats:$email", 0, -1)
                    .flatMap { chatID ->
                        removeUserFromChat(email, chatID)
                    }
                    .then(redisTemplate.delete("user_chats:$email"))
                Mono.zip(deleteUserStatus, deleteUserInChats).then()
            } else {
                decrementSessionQuantity(email)
                    .then()
            }
        }
}

fun createActiveStatus(email: String): Mono<Boolean> {
        return redisOpsForValue.set("user_status:$email", "1")
            .flatMap {
                updateTTL("user_status:$email")
            }
    }

fun incrementSessionQuantity(email: String): Mono<Boolean> {
        return redisOpsForValue.increment("user_status:$email")
            .flatMap {
                updateTTL("user_status:$email")
            }
    }

fun decrementSessionQuantity(email: String): Mono<Boolean> {
    return redisOpsForValue.decrement("user_status:$email")
        .flatMap {
            updateTTL("user_status:$email")
        }
}
*/

/*
@Repository
class StatusRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val redisSetOps = redisTemplate.opsForSet()
    private val redisValueOps = redisTemplate.opsForValue()

    fun setActive(email: String): Mono<Boolean> {
        return redisValueOps.set("status:$email", "active")
    }

    fun getStatus(email: String): Mono<String> {
        return redisValueOps.get("status:$email")
            .switchIfEmpty(Mono.just("inactive"))
    }

    fun setUserTracking(emailToSet: String, userWhoTracking: String): Mono<Boolean> {
        val addUserTracking = redisSetOps.add("tracking:$userWhoTracking", emailToSet)
        val addUserToBeTracked = redisSetOps.add("tracked:$emailToSet", userWhoTracking)

        return Mono.zip(addUserTracking, addUserToBeTracked)
            .map { tuple ->
                // Возвращаем true, если обе операции успешны
                tuple.t1 > 0 && tuple.t2 > 0
            }
            .flatMap { setActive(emailToSet) }
    }

    fun setUserTracking(emailToSet: String, usersWhoTracking: Set<String>): Mono<Boolean> {
        val usersWhoTrackingFlux = Flux.fromIterable(usersWhoTracking)

        return usersWhoTrackingFlux.parallel() // Включаем параллельную обработку
            .runOn(Schedulers.parallel()) // Указываем планировщик
            .flatMap { emailWhoTracks ->
                val addUserTracking = redisSetOps.add("tracking:$emailWhoTracks", emailToSet)
                val addUserToBeTracked = redisSetOps.add("tracked:$emailToSet", emailWhoTracks)

                Mono.zip(addUserTracking, addUserToBeTracked)
                    .map { tuple ->
                        tuple.t1 > 0 && tuple.t2 > 0
                    }
                    .doOnError { error ->
                        println("Error adding tracking for $emailWhoTracks: ${error.message}")
                    }
            }
            .sequential() // Возвращаемся к последовательному потоку
            .all { it } // Проверяем, что все операции успешны
            .doOnSuccess { success ->
                if (success) {
                    println("All tracking relationships added successfully.")
                } else {
                    println("Some tracking relationships failed to add.")
                }
            }
            .flatMap { setActive(emailToSet) }
    }

    fun setInactive(emailToRemove: String): Mono<Boolean> {
        return redisSetOps.members("tracked:$emailToRemove")
            .flatMap { emailToReduceTracking ->
                redisSetOps.remove("tracking:$emailToReduceTracking", emailToRemove)
                    .doOnSuccess { removed ->
                        if (removed > 0) {
                            println("Removed $emailToRemove from tracking:$emailToReduceTracking")
                        } else {
                            println("Failed to remove $emailToRemove from tracking:$emailToReduceTracking")
                        }
                    }
                    .doOnError { error ->
                        println("Error removing $emailToRemove from tracking:$emailToReduceTracking: ${error.message}")
                    }
            }
            .then(redisSetOps.delete("tracked:$emailToRemove"))
            .doOnSuccess { deleted ->
                if (deleted) {
                    println("Deleted tracked:$emailToRemove")
                } else {
                    println("Failed to delete tracked:$emailToRemove")
                }
            }
            .then (redisSetOps.members("tracking:$emailToRemove")
            .flatMap { emailWhoTracked: String ->
                redisSetOps.remove("tracked:$emailWhoTracked", emailToRemove)
                    .doOnSuccess { removed ->
                        if (removed > 0) {
                            println("Removed $emailToRemove from tracked:$emailWhoTracked")
                        } else {
                            println("Failed to remove $emailToRemove from tracked:$emailWhoTracked")
                        }
                    }
                    .doOnError { error ->
                        println("Error removing $emailToRemove from tracked:$emailWhoTracked : ${error.message}")
                    }

            }
            .then())
            .then(redisValueOps.delete("status:$emailToRemove"))
            .doOnSuccess { deleted ->
                if (deleted) {
                    println("Deleted status:$emailToRemove")
                } else {
                    println("Failed to delete status:$emailToRemove")
                }
            }
            .thenReturn(true) // Возвращаем true, если все операции выполнены
            .onErrorReturn(false) // Возвращаем false в случае ошибки
    }

    fun fetchUsersTracking(email: String): Mono<List<String>> {
        return redisSetOps.members("tracked:$email").collectList()

    }
}
*/
/*
fun removeUserTracking(emailToRemove: String): Mono<Boolean> {
        return redisSetOps.members("tracked:$emailToRemove")
            .flatMap { emailToReduceTracking ->
                redisSetOps.remove("tracking:$emailToReduceTracking", emailToRemove)
            }
            .then(redisSetOps.delete("tracked:$emailToRemove"))
            .then(redisValueOps.delete("status:$emailToRemove"))
    }
 */

/*
// тут нужно послать запрос в user-service, чтобы понять, какие контакты есть у пользователя,
// и какие из них активны
fun addUserTracking(emailToAdd: String, trackingUsers: Set<String>): Mono<Boolean> {
    val users = Flux.fromIterable(trackingUsers)

    return users.flatMap { emailWhoTracks ->
        // Добавляем emailToAdd в tracking:emailWhoTracks
        val addUserTracking = redisSetOps.add("tracking:$emailWhoTracks", emailToAdd)

        // Добавляем emailWhoTracks в tracked:emailToAdd
        val addUserToBeTracked = redisSetOps.add("tracked:$emailToAdd", emailWhoTracks)

        // Комбинируем результаты двух операций
        Mono.zip(addUserTracking, addUserToBeTracked)
            .map { tuple ->
                // Возвращаем true, если обе операции успешны
                tuple.t1 > 0 && tuple.t2 > 0
            }
    }.all { it }
}
*/