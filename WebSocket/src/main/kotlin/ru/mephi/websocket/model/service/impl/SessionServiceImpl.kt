package ru.mephi.websocket.model.service.impl

import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.dao.SessionRepository
import ru.mephi.websocket.model.service.SessionMap
import ru.mephi.websocket.model.service.SessionService
import java.util.UUID

@Service
class SessionServiceImpl(
    private val sessionRepository: SessionRepository,
    private val sessionMap: SessionMap
): SessionService {
    override fun addSession(userId: UUID, session: WebSocketSession): Mono<Void> {
        sessionMap.addSession(session)
        return sessionRepository.addSession(userId, session.id)
    }

    override fun removeSession(userId: UUID, sessionId: String): Mono<Void> {
        sessionMap.removeSession(sessionId)
        return sessionRepository.removeSession(userId, sessionId)
    }

    override fun getAllSessions(userId: UUID): Flux<String> {
        return sessionRepository.getAllSessions(userId)
    }

    override fun removeAllSessions(userId: UUID): Mono<Void> {
        return sessionRepository.getAllSessions(userId)
            .map { sessionId: String ->
                sessionMap.removeSession(sessionId)
            }
            .then( Mono.defer {
                sessionRepository.removeAllSessions(userId)
            })
    }
}