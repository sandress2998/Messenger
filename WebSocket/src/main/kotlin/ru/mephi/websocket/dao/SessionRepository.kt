package ru.mephi.websocket.dao

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class SessionRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val reactiveSetOps = reactiveRedisTemplate.opsForSet()

    fun addSession(email: String, sessionId: String): Mono<Long> {
        return reactiveSetOps.add(email, sessionId)
    }

    fun removeSession(email: String, sessionId: String): Mono<Long> {
        return reactiveSetOps.remove(email, sessionId)
    }

    fun getAllSessions(email: String): Flux<String> {
        return reactiveSetOps.members(email)
    }

    fun removeAllSessions(email: String): Mono<Boolean> {
        return reactiveSetOps.delete(email)
    }
}