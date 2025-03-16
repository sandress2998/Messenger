package ru.mephi.websocket.handler

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.websocket.receive.BaseReceiveNotification
import ru.mephi.websocket.model.dto.websocket.receive.ChatActivityChangeIngoingNotification
import ru.mephi.websocket.model.service.SessionService
import ru.mephi.websocket.model.service.WebSocketNotificationProcessor

@Component
class SimpleWebSocketHandler(
    private val sessionService: SessionService,
    private val webSocketNotificationProcessor: WebSocketNotificationProcessor
): WebSocketHandler {
    private val objectMapper: JsonMapper = jacksonMapperBuilder()
        .addModule(kotlinModule())
        .build()

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

        // Обработка сообщений
        return sessionService.addSession(email, session)
            .doOnSuccess {
                println("Successfully added session.")
            }
            .doOnError { error ->
                println("Error during Mono.zip: ${error.message}")
            }
            .then ( Mono.defer {
                session.receive()
                    .doOnNext { notification ->
                        println("Received message from $email: ${notification.payloadAsText}")
                    }
                    .flatMap { notification ->
                        Mono.fromCallable {
                            objectMapper.readValue<BaseReceiveNotification>(notification.payloadAsText)
                        }
                        .onErrorResume { error ->
                            // Обработка ошибки десериализации
                            println("Failed to deserialize message: ${error.message}")
                            Mono.empty() // Продолжаем обработку следующих сообщений
                        }
                        .flatMap { jsonNotification ->
                            when (jsonNotification) {
                                is ChatActivityChangeIngoingNotification -> {
                                    webSocketNotificationProcessor.processActivityStatusNotification(
                                        jsonNotification, email
                                    )
                                }
                                else -> {
                                    println("Нет подходящего класса для json-десериализации")
                                    Mono.empty()
                                }
                            }
                            .then(
                                session.send(Flux.just(session.textMessage("Echo: ${notification.payloadAsText}")))
                            )
                        }
                    }
                    .then()
            } )
            .then(session.close())
            .then( Mono.defer {
                println("Removing session $sessionId")
                sessionService.removeSession(email, sessionId)
            } )
            .then()
            .onErrorResume { error ->
                println("Error during session removal or status update: ${error.message}")
                session.close()
            }
        }
}
