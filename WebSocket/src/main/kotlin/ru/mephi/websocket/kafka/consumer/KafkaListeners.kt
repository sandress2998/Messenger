package ru.mephi.websocket.kafka.consumer

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.mephi.websocket.model.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.model.service.ActivityStatusService

@Component
class KafkaListeners(
    private val activityStatusService: ActivityStatusService
) {

    private val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

    @KafkaListener(topics = ["activity-from-presence-to-ws"], groupId = "websocket-service", containerFactory = "activityMessageListenerContainerFactory")
    suspend fun listener(message: String) {
        val chatActivityChangeIngoingMessage = objectMapper.readValue<ChatActivityChangeIngoingMessage>(message)
        println("Listener received: email = ${chatActivityChangeIngoingMessage.email}, " +
            " status = ${chatActivityChangeIngoingMessage.status}," +
                " receiver = ${chatActivityChangeIngoingMessage.receiver} :)")
        activityStatusService.sendStatusUpdateNotification(chatActivityChangeIngoingMessage).awaitSingleOrNull()
    }
}