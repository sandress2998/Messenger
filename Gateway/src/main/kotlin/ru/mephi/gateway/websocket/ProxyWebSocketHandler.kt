package ru.mephi.gateway.websocket

import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.gateway.security.JwtService
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*

class ProxyWebSocketHandler(
    private val webSocketClient: WebSocketClient,
    private val jwtService: JwtService
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        val token = session.handshakeInfo.headers["Authorization"]
            ?.firstOrNull()
            ?.removePrefix("Bearer ")

        if (token == null) {
            // Закрываем сессию, если токен отсутствует
            return session.send(Flux.just(session.textMessage("Token is missing")))
                .then(Mono.defer {
                    session.close()
                })
        }

        // Парсим JWT-токен и извлекаем время жизни
        val claims = jwtService.validateToken(token)
        val expirationTime = claims.expiration.toInstant() // Время истечения токена
        val currentTime = Instant.now() // Текущее время

        // Вычисляем оставшееся время жизни токена
        val remainingTime = Duration.between(currentTime, expirationTime)

        if (remainingTime.isNegative) {
            return session.close() // Закрываем сессию, если токен уже истёк
        }

        val userId: String = claims.subject

        println("Token is ok. UserId: $userId, expirationTime: $expirationTime.")

        // Указываем URI целевого микросервиса
        val targetUri = URI.create("ws://websocket-service:8092/ws")

        val headers = HttpHeaders()
        headers.add("X-UserId", userId)

        // Устанавливаем соединение с целевым микросервисом
        return webSocketClient.execute(targetUri, headers) { targetSession ->
            println("Connected to target WebSocket service")

            // Клиент -> Целевой микросервис
            val clientToTarget = session.receive()
                .flatMap { message ->
                    println("Message from client: ${message.payloadAsText}")
                    // Увеличиваем счетчик ссылок на сообщение
                    val retainedMessage = message.retain()
                    targetSession.send(Flux.just(retainedMessage))
                        .doOnSuccess { println("Message sent to target") }
                        .doOnError { error -> println("Error sending to target: ${error.message}") }
                }
                .doOnError { error -> println("Error in clientToTarget: ${error.message}") }
                .doOnComplete { println("Client to target stream completed") }


            // Целевой микросервис -> Клиент
            val targetToClient = targetSession.receive()
                .flatMap { message ->
                    println("Message from target: ${message.payloadAsText}")
                    // Увеличиваем счетчик ссылок на сообщение
                    val retainedMessage = message.retain()
                    session.send(Flux.just(retainedMessage))
                        .doOnSuccess { println("Message sent to client") }
                        .doOnError { error -> println("Error sending to client: ${error.message}") }
                }
                .doOnError { error -> println("Error in targetToClient: ${error.message}") }
                .doOnComplete { println("Target to client stream completed") }


            Flux.firstWithSignal(clientToTarget, targetToClient)
                .timeout(remainingTime)
                .then(Mono.defer {
                    println("Connection completed, closing sessions")
                    session.close()
                        .onErrorResume { error ->
                            println("Error closing session: ${error.message}")
                            Mono.empty() // Игнорируем ошибку
                        }
                        .then(targetSession.close())
                        .onErrorResume { error ->
                            println("Error closing targetSession: ${error.message}")
                            Mono.empty() // Игнорируем ошибку
                        }
                })
                .onErrorResume { errorInWebSocketHandler ->
                    println("Error in WebSocket handler: ${errorInWebSocketHandler.message}")
                    session.close(CloseStatus.SERVER_ERROR)
                        .onErrorResume { error ->
                            println("Error closing session: ${error.message}")
                            Mono.empty() // Игнорируем ошибку
                        }
                        .then(targetSession.close())
                        .onErrorResume { error ->
                            println("Error closing targetSession: ${error.message}")
                            Mono.empty() // Игнорируем ошибку
                        }
                }
        }.onErrorResume { error ->
            println("Failed to connect to target WebSocket service: ${error.message}")
            session.send(Flux.just(session.textMessage("Failed to connect to target service")))
                .then(session.close(CloseStatus.SERVER_ERROR))
        }
    }
}