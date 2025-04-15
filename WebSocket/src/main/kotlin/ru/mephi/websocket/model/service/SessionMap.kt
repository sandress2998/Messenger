package ru.mephi.websocket.model.service

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class SessionMap {
    private val sessionMap = ConcurrentHashMap<String, WebSocketSession>()

    fun addSession(session: WebSocketSession) {
        sessionMap[session.id] = session
    }

    fun removeSession(sessionId: String) {
        sessionMap.remove(sessionId)
    }

    fun getSession(sessionId: String): WebSocketSession? {
        return sessionMap[sessionId]
    }
}