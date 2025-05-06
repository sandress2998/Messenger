package ru.mephi.websocket.model.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.mephi.websocket.dto.kafka.receive.*
import ru.mephi.websocket.model.dto.kafka.receive.*

@Component
class KafkaListeners(
    private val webSocketProducerService: WebSocketProducerService,
    private val objectMapper: ObjectMapper
) {

    @KafkaListener(topics = ["activity-status-notification-to-members"], groupId = "websocket-service", containerFactory = "activityMessageListenerContainerFactory")
    suspend fun activityListener(message: String) {
        val chatActivityChangeIngoingMessage = objectMapper.readValue<ChatActivityChangeIngoingMessage>(message)
        println("Listener received: $chatActivityChangeIngoingMessage :)")
        webSocketProducerService.sendActivityStatusNotification(chatActivityChangeIngoingMessage).awaitSingleOrNull()
    }

    @KafkaListener(topics = ["chat-action"], groupId = "websocket-service")
    suspend fun chatActionListener(message: String) {
        val chatActionIngoingMessage = objectMapper.readValue<ChatActionIngoingMessage>(message)
        println("Listener received: = $chatActionIngoingMessage:)")
        webSocketProducerService.sendChatActionNotification(chatActionIngoingMessage).awaitSingleOrNull()
    }

    @KafkaListener(topics = ["chat-member-action"], groupId = "websocket-service")
    suspend fun chatMemberActionListener(message: String) {
        val chatMemberActionIngoingMessage = objectMapper.readValue<ChatMemberActionIngoingMessage>(message)
        println("Listener received: = $chatMemberActionIngoingMessage :)")
        webSocketProducerService.sendChatMemberActionNotification(chatMemberActionIngoingMessage).awaitSingleOrNull()
    }

    @KafkaListener(topics = ["message-action"], groupId = "websocket-service")
    suspend fun messageActionListener(message: String) {
        val modules = objectMapper.registeredModuleIds
        println("Registered modules: $modules")

        val messageActionIngoingMessage = objectMapper.readValue<MessageActionIngoingMessage>(message)
        println("Listener received: category = $messageActionIngoingMessage + messageInfo: ${messageActionIngoingMessage.messageInfo} :)")
        webSocketProducerService.sendMessageActionNotification(messageActionIngoingMessage).awaitSingleOrNull()
    }

    @KafkaListener(topics = ["user-action"], groupId = "websocket-service")
    suspend fun userActionListener(message: String) {
        val userActionIngoingMessage = objectMapper.readValue<UserActionIngoingMessage>(message)
        println("Listener received: = $userActionIngoingMessage :)")
        webSocketProducerService.sendUserActionNotification(userActionIngoingMessage).awaitSingleOrNull()
    }
}