package ru.mephi.websocket.model.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.dao.SessionRepository
import ru.mephi.websocket.model.service.SessionMap
import ru.mephi.websocket.model.service.SessionService
import java.util.*

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

    override fun doSessionsExist(userId: UUID): Mono<Boolean> {
        return sessionRepository.getAllSessions(userId)
            .collectList()
            .map { list ->
                list.size > 0
            }
    }

    override fun sendNotification(userId: UUID, message: Any): Mono<Void> {
        val notification = objectMapper.writeValueAsString(message)
        return getAllSessions(userId)
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

    companion object {
        private val objectMapper = ObjectMapper()
    }
}