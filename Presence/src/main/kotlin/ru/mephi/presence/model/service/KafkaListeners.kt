package ru.mephi.presence.model.service

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.kafka.ActivityChangeEvent

@Component
class KafkaListeners(
    private val statusMessaging: StatusMessaging
) {
    val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()


    @KafkaListener(topics = ["activity-status-change"], groupId = "presence-service", containerFactory = "messageKafkaListenerContainerFactory")
    fun chatActivityListener(message: String): Mono<Void> {
        // Десериализуем JSON
        val activityChangeEvent = objectMapper.readValue<ActivityChangeEvent>(message)

        println("Received message: $activityChangeEvent")
        return statusMessaging.handleChatActivityMessage(activityChangeEvent)
    }
}

/*
@Component
class KafkaListeners(
    private val statusMessaging: StatusMessaging
) {
    @KafkaListener(topics = ["activity-from-ws-to-presence"], groupId = "presence-service", containerFactory = "messageKafkaListenerContainerFactory")
    fun listener(message: StatusChangeEvent): Mono<Void> {
        return statusMessaging.handleActivityMessage(message)
    }
}
 */