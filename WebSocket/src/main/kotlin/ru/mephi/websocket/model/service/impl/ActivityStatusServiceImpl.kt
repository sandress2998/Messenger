package ru.mephi.websocket.model.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.kafka.dto.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.kafka.dto.send.ChatActivityChangeOutgoingMessage
import ru.mephi.websocket.model.mapper.ActivityStatusMapper
import ru.mephi.websocket.model.service.ActivityStatusService
import ru.mephi.websocket.model.service.SessionMap
import ru.mephi.websocket.model.service.SessionService

// По идее осталось реализовать только это

@Service
class ActivityStatusServiceImpl (
   private val sessionService: SessionService,
    private val sessionMap: SessionMap,
    private val mapper: ActivityStatusMapper,
    private val messageKafkaTemplate: KafkaTemplate<String, ChatActivityChangeOutgoingMessage>
): ActivityStatusService {
    override fun sendStatusUpdateNotification(message: ChatActivityChangeIngoingMessage): Mono<Void> {
        val receiver = message.receiver

        val notification = objectMapper.writeValueAsString(mapper.messageAsNotification(message))
        return sessionService.getAllSessions(receiver)
            .flatMap { sessionId ->
                println("Current sessionId to notify: $sessionId")
                val session = sessionMap.getSession(sessionId)
                if (session == null) {
                    println("By some reason session $sessionId is null.")
                    return@flatMap Mono.empty<Void>()
                }

                println("We're trying to send notification to session $sessionId")
                // Создаем текстовое сообщение и отправляем его через сессию
                val textMessage = session.textMessage(notification)
                session.send(Mono.just(textMessage)) // Отправляем сообщение
            }
            .then()
    }

    override fun sendStatusUpdateMessage(message: ChatActivityChangeOutgoingMessage): Mono<Void> {
        return Mono.fromFuture(
            messageKafkaTemplate.send("activity-from-ws-to-presence", message)
        ).then()
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}