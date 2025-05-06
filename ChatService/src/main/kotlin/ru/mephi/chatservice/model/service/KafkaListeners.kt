package ru.mephi.chatservice.model.service

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.dto.kafka.ActivityChangeIngoingMessage
import ru.mephi.chatservice.model.dto.kafka.UserActionForChatMembersIngoingMessage

@Component
class KafkaListeners(
    private val activityService: ActivityService,
    private val chatService: ChatService
) {
    private val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

    @KafkaListener(topics = ["activity-status-change"], groupId = "chat-service")
    fun activityListener(message: String): Mono<Void> {
        val activityChangeIngoingMessage = objectMapper.readValue<ActivityChangeIngoingMessage>(message)
        println("Listener received: $message :)")
        return activityService.handleActivityChangeMessage(activityChangeIngoingMessage)
    }

    @KafkaListener(topics = ["user-action-for-chat-members"], groupId = "chat-service")
    fun userActionListener(message: String): Mono<Void> {
        val userActionIngoingMessage = objectMapper.readValue<UserActionForChatMembersIngoingMessage>(message)
        println("Listener received: $message :)")
        return chatService.handleUserActionNotification(userActionIngoingMessage)
    }
}