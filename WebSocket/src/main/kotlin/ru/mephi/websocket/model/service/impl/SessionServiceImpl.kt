package ru.mephi.websocket.model.service.impl

import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.dao.SessionRepository
import ru.mephi.websocket.model.service.SessionMap
import ru.mephi.websocket.model.service.SessionService

@Service
class SessionServiceImpl(
    private val sessionRepository: SessionRepository,
    private val sessionMap: SessionMap
): SessionService {
    override fun addSession(email: String, session: WebSocketSession): Mono<Long> {
        sessionMap.addSession(session)
        return sessionRepository.addSession(email, session.id)
    }

    override fun removeSession(email: String, sessionId: String): Mono<Long> {
        sessionMap.removeSession(sessionId)
        return sessionRepository.removeSession(email, sessionId)
    }

    override fun getAllSessions(email: String): Flux<String> {
        return sessionRepository.getAllSessions(email)
    }

    override fun removeAllSessions(email: String): Mono<Boolean> {
        return sessionRepository.getAllSessions(email)
            .map { sessionId: String ->
                sessionMap.removeSession(sessionId)
            }
            .then( Mono.defer {
                sessionRepository.removeAllSessions(email)
            })
    }
}