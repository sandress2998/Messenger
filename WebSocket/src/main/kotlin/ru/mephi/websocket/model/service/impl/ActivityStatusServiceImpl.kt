package ru.mephi.websocket.model.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.kafka.receive.UserStatusChangeBroadcast
import ru.mephi.websocket.model.dto.kafka.send.UserStatusChangeEvent
import ru.mephi.websocket.model.dto.websocket.send.UserStatusChangeNotification
import ru.mephi.websocket.model.service.ActivityStatusService
import ru.mephi.websocket.model.service.SessionMap
import ru.mephi.websocket.model.service.SessionService

// По идее осталось реализовать только это

@Service
class ActivityStatusServiceImpl (
   private val sessionService: SessionService,
    private val sessionMap: SessionMap,
    private val messageKafkaTemplate: KafkaTemplate<String, UserStatusChangeEvent>
): ActivityStatusService {
    override fun sendStatusUpdateNotification(message: UserStatusChangeBroadcast): Mono<Void> {
        val email = message.email
        val status = message.status
        val notification = objectMapper.writeValueAsString(UserStatusChangeNotification(email, status))
        return sessionService.getAllSessions(email)
            .flatMap { sessionId ->
                Mono.fromCallable {
                    val session = sessionMap.getSession(sessionId)
                    session?.send(Mono.just(session.textMessage(notification)))
                }
            }
            .then()
    }

    override fun sendStatusUpdateMessage(message: UserStatusChangeEvent): Mono<Void> {
        return Mono.fromFuture(
            messageKafkaTemplate.send("activity-from-ws-to-presence", message)
        ).then()
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}