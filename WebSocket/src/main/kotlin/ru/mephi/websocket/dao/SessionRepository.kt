package ru.mephi.websocket.dao

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.property.SecurityProperties
import java.time.Duration

@Repository
class SessionRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val securityProperties: SecurityProperties
) {
    private val reactiveSetOps = reactiveRedisTemplate.opsForSet()
    private val jwtTTL = Duration.ofMinutes(securityProperties.jwtTimeoutInMinutes)


    fun addSession(email: String, sessionId: String): Mono<Void> {
        return reactiveSetOps.add("sessions:$email", sessionId)
            .flatMap {
                reactiveRedisTemplate.expire(email, jwtTTL)
            }
            .then()
    }

    fun removeSession(email: String, sessionId: String): Mono<Void> {
        return reactiveSetOps.remove("sessions:$email", sessionId)
            .then()
    }

    fun getAllSessions(email: String): Flux<String> {
        return reactiveSetOps.members("sessions:$email")
    }

    fun removeAllSessions(email: String): Mono<Void> {
        return reactiveSetOps.delete("sessions:$email")
            .then()
    }
}