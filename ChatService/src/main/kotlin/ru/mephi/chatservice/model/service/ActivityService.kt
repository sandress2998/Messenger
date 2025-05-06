package ru.mephi.chatservice.model.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.ActivityStatus
import ru.mephi.chatservice.model.dto.kafka.ActivityChangeIngoingMessage
import ru.mephi.chatservice.model.dto.kafka.ActivityChangeOutgoingMessage
import ru.mephi.chatservice.model.dto.rest.UserId
import ru.mephi.chatservice.database.entity.ChatMember
import ru.mephi.chatservice.database.repository.ActivityRepository
import ru.mephi.chatservice.database.repository.ChatMembersRepository
import ru.mephi.chatservice.webclient.PresenceService
import java.util.*

@Service
class ActivityService (
    private val activityRepository: ActivityRepository,
    private val chatMembersRepository: ChatMembersRepository,
    private val kafkaTemplate: KafkaTemplate<String, ActivityChangeOutgoingMessage>,
    private val presenceService: PresenceService
) {
    fun handleActivityChangeMessage(message: ActivityChangeIngoingMessage): Mono<Void> {
        return when (message.status) {
            ActivityStatus.ACTIVE -> handleActiveUser(message.userId)
            ActivityStatus.INACTIVE -> handleInactiveUser(message.userId)
        }
    }

    fun getActivityStatus(userId: UUID, chatId: UUID): Mono<ActivityStatus> {
        return activityRepository.isMemberActive(userId, chatId)
            .map { isActive ->
                when (isActive) {
                    true -> ActivityStatus.ACTIVE
                    false -> ActivityStatus.INACTIVE
                }
            }
    }

    fun deleteFromChat(userId: UUID, chatId: UUID): Mono<Void> {
        return activityRepository.deleteFromChat(userId, chatId)
    }

    fun addToChat(userId: UUID, chatId: UUID): Mono<Boolean> {
        return activityRepository.addToChat(userId, chatId)
    }

    fun deleteChat(chatId: UUID): Mono<Void> {
        return activityRepository.deleteChat(chatId)
    }

    // может быть случай, что пользователь вообще не имеет чатов. Тогда надо сделать запрос на presence-service
    fun getUserActivityStatus(userId: UUID): Mono<ActivityStatus> {
        return chatMembersRepository.findRandomChatIdsByUserId(userId)
            .flatMap { chatId ->
                activityRepository.getActiveChatMembers(chatId)
                    .collectList()
                    .map { list -> list.contains(userId) }
            }
            .collectList()
            .flatMap { list ->
                if (list.size == 1) {
                    println("we're sending a request if user is active")
                    presenceService.isUserActive(userId)
                } else {
                    println("size = ${list.size}")
                    when (list.contains(true)) {
                        true -> Mono.just(ActivityStatus.ACTIVE)
                        false -> Mono.just(ActivityStatus.INACTIVE)
                    }
                }
            }
    }

    fun getActiveMembers(chatId: UUID): Mono<List<UUID>> {
        return activityRepository.getActiveChatMembers(chatId)
            .collectList()
    }

    fun getActiveUsersInChat(chatId: UUID): Flux<UserId> {
        return activityRepository.getActiveChatMembers(chatId)
            .map { UserId(it) }
    }


    private fun handleActiveUser(userId: UUID): Mono<Void> {
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMember: ChatMember ->
                val chatId = chatMember.chatId
                val memberId = chatMember.id!!
                    activityRepository.addToChat(userId, chatId)
                        .flatMap { isAdded ->
                            if (isAdded) {
                                notifyMembersAboutActivityChange(chatId, memberId, ActivityStatus.ACTIVE)
                                .then()
                            } else {
                                Mono.empty()
                            }
                        }
            }
            .then()
    }

    private fun handleInactiveUser(userId: UUID): Mono<Void> {
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMember: ChatMember ->
                val chatId = chatMember.chatId
                val memberId = chatMember.id!!
                activityRepository.deleteFromChat(userId, chatId)
                    .thenMany(notifyMembersAboutActivityChange(chatId, memberId, ActivityStatus.INACTIVE))
            }
            .then()
    }

    private fun notifyMembersAboutActivityChange(
        chatId: UUID,
        memberId: UUID,
        activityStatus: ActivityStatus
    ): Flux<Void> {
        return activityRepository.getActiveChatMembers(chatId)
            .flatMap { userId: UUID ->
                sendActivityMessage(ActivityChangeOutgoingMessage(userId, chatId, memberId, activityStatus))
            }
    }

    private fun sendActivityMessage(message: ActivityChangeOutgoingMessage): Mono<Void> {
        return Mono.fromCallable {
            MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "activity-status-notification-to-members")
                .build()
        }.flatMap { sendMessage ->
            Mono.create { sink ->
                try {
                    val future = kafkaTemplate.send(sendMessage)
                    future.whenComplete { result, ex ->
                        if (ex != null) {
                            println("Failed to send: ${ex.message}")
                            sink.error(ex)
                        } else {
                            println("Successfully sent: $sendMessage")
                            sink.success()
                        }
                    }
                } catch (e: Exception) {
                    sink.error(e)
                }
            }
        }
    }
}