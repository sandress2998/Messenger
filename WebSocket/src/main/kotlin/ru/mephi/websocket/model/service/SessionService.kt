package ru.mephi.websocket.model.service

import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface SessionService {
    fun addSession(userId: UUID, session: WebSocketSession): Mono<Void>

    fun removeSession(userId: UUID, sessionId: String): Mono<Void>

    fun getAllSessions(userId: UUID): Flux<String>

    fun removeAllSessions(userId: UUID): Mono<Void>

    fun doSessionsExist(userId: UUID): Mono<Boolean>

    fun sendNotification(userId: UUID, message: Any): Mono<Void>
}