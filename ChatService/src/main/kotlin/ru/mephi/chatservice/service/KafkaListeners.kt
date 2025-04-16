package ru.mephi.chatservice.service

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.dto.kafka.ActivityChangeIngoingMessage

@Component
class KafkaListeners(
    private val activityService: ActivityService
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
}