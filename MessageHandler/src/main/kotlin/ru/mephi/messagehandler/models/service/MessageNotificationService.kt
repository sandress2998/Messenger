package ru.mephi.messagehandler.models.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.MessageAction
import ru.mephi.messagehandler.models.dto.kafka.MessageActionOutgoingMessage
import ru.mephi.messagehandler.models.dto.kafka.MessageInfo
import ru.mephi.messagehandler.webclient.ChatService
import java.util.*

@Service
class MessageNotificationService (
    private val chatService: ChatService,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun notifyChatMembersAboutMessageAction(memberId: UUID, chatId: UUID, messageId: UUID, action: MessageAction, message: MessageInfo? = null): Mono<Void> {
        return chatService.getActiveUsersInChat(chatId)
            .flatMap { userId ->
                notifyAboutMessageAction(userId, memberId, chatId, messageId, action, message)
            }
            .then()
    }

    private fun notifyAboutMessageAction(userId: UUID, memberId: UUID, chatId: UUID, messageId: UUID, action: MessageAction, messageInfo: MessageInfo? = null): Mono<Void> {
        return sendChatMemberActionMessage(
            MessageActionOutgoingMessage(
                userId, action, messageId, chatId, memberId, messageInfo
            )
        )
    }

    private fun sendChatMemberActionMessage(message: MessageActionOutgoingMessage): Mono<Void> {
        return Mono.fromCallable {
            val stringMessage = objectMapper.writeValueAsString(message)
            println("json with objectMapper: $stringMessage")
            MessageBuilder
                .withPayload(stringMessage)
                .setHeader(KafkaHeaders.TOPIC, "message-action")
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