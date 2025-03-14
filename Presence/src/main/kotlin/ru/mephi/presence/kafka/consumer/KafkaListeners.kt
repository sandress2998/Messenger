package ru.mephi.presence.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.UserStatusChangeEvent
import ru.mephi.presence.model.service.StatusMessaging

@Component
class KafkaListeners(
    private val statusMessaging: StatusMessaging
) {
    val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

    @KafkaListener(topics = ["activity-from-ws-to-presence"], groupId = "presence-service", containerFactory = "messageKafkaListenerContainerFactory")
    fun listener(message: String): Mono<Void> {
        // Десериализуем JSON
        val userStatusChangeEvent = objectMapper.readValue<UserStatusChangeEvent>(message)
        /*
        val data = objectMapper.readValue(message, Map::class.java)
        val email = data["email"] as String
        val status = data["status"] as String

        // Преобразуем в нужный класс
        val userStatusChangeEvent = UserStatusChangeEvent(email, status)
         */
        println("Received message: $userStatusChangeEvent")
        return statusMessaging.handleActivityMessage(userStatusChangeEvent)
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