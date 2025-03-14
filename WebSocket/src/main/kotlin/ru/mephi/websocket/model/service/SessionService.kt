package ru.mephi.websocket.model.service

import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SessionService {
    fun addSession(email: String, session: WebSocketSession): Mono<Long>

    fun removeSession(email: String, sessionId: String): Mono<Long>

    fun getAllSessions(email: String): Flux<String>

    fun removeAllSessions(email: String): Mono<Boolean>
}