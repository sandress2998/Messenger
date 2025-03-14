package ru.mephi.websocket.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import ru.mephi.websocket.model.dto.kafka.receive.UserStatusChangeBroadcast
import ru.mephi.websocket.model.service.ActivityStatusService

@Component
class KafkaListeners(
    private val activityStatusService: ActivityStatusService
) {

    val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

    @KafkaListener(topics = ["activity-from-presence-to-ws"], groupId = "websocket-service", containerFactory = "activityMessageListenerContainerFactory")
    suspend fun listener(message: String) {
        val userStatusChangeBroadcast = objectMapper.readValue<UserStatusChangeBroadcast>(message)
        println("Listener received: ${userStatusChangeBroadcast.email}, " +
            "${userStatusChangeBroadcast.status}, ${userStatusChangeBroadcast.receivers} :)")
        activityStatusService.sendStatusUpdateNotification(userStatusChangeBroadcast).awaitSingleOrNull()
    }
}