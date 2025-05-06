package ru.mephi.userservice.model.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.userservice.model.UserAction
import ru.mephi.userservice.model.dto.UserActionForChatMembersOutgoingMessage
import java.util.*

@Service
class UserNotificationForChatMembersService (
    private val kafkaTemplate: KafkaTemplate<String, UserActionForChatMembersOutgoingMessage>
) {
    fun notifyAboutUserAction(userId: UUID, action: UserAction): Mono<Void> {
        return sendUserActionMessage(UserActionForChatMembersOutgoingMessage(userId, action))
    }

    private fun sendUserActionMessage(message: UserActionForChatMembersOutgoingMessage): Mono<Void> {
        return Mono.fromCallable {
            MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "user-action-for-chat-members")
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