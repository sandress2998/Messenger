package ru.mephi.chatservice.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.ChatAction
import ru.mephi.chatservice.models.ChatMemberAction
import ru.mephi.chatservice.models.dto.kafka.ChatMemberOutgoingMessage
import ru.mephi.chatservice.models.dto.kafka.ChatOutgoingMessage
import ru.mephi.chatservice.models.dto.rest.ChatInfo
import ru.mephi.chatservice.models.dto.rest.MemberInfo
import ru.mephi.chatservice.repository.ActivityRepository
import java.util.*

@Service
class ChatNotificationService (
    private val kafkaTemplate: KafkaTemplate<String, ChatMemberAction>,
    private val activityRepository : ActivityRepository
) {
    fun notifyAboutChatMemberAction(chatId: UUID, memberId: UUID, action: ChatMemberAction, member: MemberInfo? = null): Mono<Void> {
        return activityRepository.getActiveChatMembers(chatId)
            .flatMap { userId ->
                val message = ChatMemberOutgoingMessage(action, userId, chatId, memberId, member)
                sendChatMemberActionMessage(message)
            }
            .then()
    }

    fun notifyAboutChatAction(chatId: UUID, action: ChatAction, updatedChat: ChatInfo? = null): Mono<Void> {
        return activityRepository.getActiveChatMembers(chatId)
            .flatMap { userId ->
                val message = ChatOutgoingMessage(action, userId, chatId, updatedChat)
                sendChatActionMessage(message)
            }
            .then()
    }

    private fun sendChatMemberActionMessage(message: ChatMemberOutgoingMessage): Mono<Void> {
        return Mono.fromCallable {
            MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "chat-member-action")
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

    private fun sendChatActionMessage(message: ChatOutgoingMessage): Mono<Void> {
        return Mono.fromCallable {
            MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "chat-action")
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