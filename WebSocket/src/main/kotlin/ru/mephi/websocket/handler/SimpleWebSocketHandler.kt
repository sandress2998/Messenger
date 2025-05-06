package ru.mephi.websocket.handler

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.kafka.send.ActivityChangeOutgoingMessage
import ru.mephi.websocket.model.service.KafkaProducerService
import ru.mephi.websocket.model.service.SessionService
import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

@Component
class SimpleWebSocketHandler(
    private val sessionService: SessionService,
    private val kafkaProducerService: KafkaProducerService
): WebSocketHandler {
    private val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

    override fun handle(session: WebSocketSession): Mono<Void> {
        println("Connection could be establish")

        // Извлечение заголовка
        val userId: UUID? = session.handshakeInfo.headers["X-UserId"]?.firstOrNull().let {
            try {
                UUID.fromString(it)
            } catch (e: IllegalArgumentException) {
                return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("UserId has incorrect format"))
            }
        }

        if (userId == null) {
            println("UserId is missing")
            return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("UserId is missing"))
        }

        session.attributes["userId"] = userId

        val sessionId = session.id

        println("Target connection established. UserId: $userId")

        // Обработка сообщений
        return sessionService.addSession(userId, session)
            .doOnSuccess {
                println("Successfully added session.")
            }
            .doOnError { error ->
                println("Error during Mono.zip: ${error.message}")
            }
            .then (
                kafkaProducerService.sendActivityStatusMessage(
                    ActivityChangeOutgoingMessage(userId, ActivityStatus.ACTIVE)
                )
                .then(session.receive()
                .doOnNext { notification ->
                    println("Received message from $userId: ${notification.payloadAsText}")
                }
                .then()
            ))
            .then(session.close())
            .then( Mono.defer {
                println("Removing session $sessionId")
                sessionService.removeSession(userId, sessionId)
            } )
            .then(sessionService.doSessionsExist(userId)
                .flatMap { exist ->
                    if (!exist) {
                        kafkaProducerService.sendActivityStatusMessage(ActivityChangeOutgoingMessage(userId, ActivityStatus.INACTIVE))
                    } else {
                        Mono.empty()
                    }
                }
            )
            .onErrorResume { error ->
                println("Error during session removal or status update: ${error.message}")
                session.close()
            }
        }
}
