package ru.mephi.websocket.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.kafka.send.UserStatusChangeEvent
import ru.mephi.websocket.model.service.ActivityStatusService
import ru.mephi.websocket.model.service.SessionService

@Component
class SimpleWebSocketHandler(
    private val sessionService: SessionService,
    private val activityStatusService: ActivityStatusService
): WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        println("Connection could be establish")

        // Извлечение заголовка
        val email = session.handshakeInfo.headers["X-Email"]?.firstOrNull()

        if (email == null) {
            println("Email is missing")
            return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Email is missing"))
        }

        // Проверка и извлечение email из токена
        session.attributes["email"] = email

        val sessionId = session.id

        println("Target connection established. Email: $email")

        val addSessionMono: Mono<Long> = sessionService.addSession(email, session)
        val activeStatusMessage = UserStatusChangeEvent(email, "active")
        val inactiveStatusMessage = UserStatusChangeEvent(email, "inactive")
        val sendActivityStatusMessage = activityStatusService.sendStatusUpdateMessage(activeStatusMessage)

        // Обработка сообщений
        return Mono.zip(addSessionMono, sendActivityStatusMessage)
            .doOnSuccess {
                println("Successfully added session and sent status: $it")
            }
            .doOnError { error ->
                println("Error during Mono.zip: ${error.message}")
            }
            .then ( Mono.defer {
                println("Successfully added session: ${session.id}")
                session.receive()
                    .doOnNext { message ->
                        println("Received message from $email: ${message.payloadAsText}")
                    }
                    .flatMap { message ->
                        session.send(Flux.just(session.textMessage("Echo: ${message.payloadAsText}")))
                    }
                    .then()
            } )
            .then(
                session.close()
                .then( Mono.defer {
                    println("Removing session $sessionId")
                    sessionService.removeSession(email, sessionId)
                } )
                .then( Mono.defer {
                    activityStatusService.sendStatusUpdateMessage(inactiveStatusMessage)
                } )
                .onErrorResume { error ->
                    println("Error during session removal or status update: ${error.message}")
                    activityStatusService.sendStatusUpdateMessage(inactiveStatusMessage)
                        .onErrorResume { innerError ->
                            println("Failed to send inactive status: ${innerError.message}")
                            Mono.empty()
                        }
                }
            )
        }
}
