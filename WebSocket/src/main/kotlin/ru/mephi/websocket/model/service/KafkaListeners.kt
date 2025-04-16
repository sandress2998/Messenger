package ru.mephi.websocket.model.service

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatMemberActionIngoingMessage

@Component
class KafkaListeners(
    private val activityStatusService: ActivityStatusService,
    private val chatService: ChatService,
    private val chatMemberService: ChatMemberService
) {

    private val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

    @KafkaListener(topics = ["activity-status-notification-to-members"], groupId = "websocket-service", containerFactory = "activityMessageListenerContainerFactory")
    suspend fun activityListener(message: String) {
        val chatActivityChangeIngoingMessage = objectMapper.readValue<ChatActivityChangeIngoingMessage>(message)
        println("Listener received: userId = ${chatActivityChangeIngoingMessage.userId}, " +
            " status = ${chatActivityChangeIngoingMessage.status}," +
                " memberId = ${chatActivityChangeIngoingMessage.memberId} +" +
                " chatId = ${chatActivityChangeIngoingMessage.chatId} :)")
        activityStatusService.sendStatusUpdateNotification(chatActivityChangeIngoingMessage).awaitSingleOrNull()
    }

    @KafkaListener(topics = ["chat-action"], groupId = "websocket-service")
    suspend fun chatActionListener(message: String) {
        val chatActionIngoingMessage = objectMapper.readValue<ChatActionIngoingMessage>(message)
        println("Listener received: category = $chatActionIngoingMessage:)")
        chatService.sendChatActionNotification(chatActionIngoingMessage).awaitSingleOrNull()
    }

    @KafkaListener(topics = ["chat-member-action"], groupId = "websocket-service")
    suspend fun chatMemberActionListener(message: String) {
        val chatMemberActionIngoingMessage = objectMapper.readValue<ChatMemberActionIngoingMessage>(message)
        println("Listener received: category = $chatMemberActionIngoingMessage :)")
        chatMemberService.sendChatMemberActionNotification(chatMemberActionIngoingMessage).awaitSingleOrNull()
    }
}