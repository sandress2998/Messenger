package ru.mephi.websocket.dao

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.websocket.property.SecurityProperties
import java.time.Duration
import java.util.*

@Repository
class SessionRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val securityProperties: SecurityProperties,
    private val registry: MeterRegistry
) {
    private var cachedSessionCount = 0.0

    init {
        Flux.interval(Duration.ofSeconds(30))  // Обновление каждые 30 секунд
            .flatMap { getCurrentSessionsQuantity() }
            .doOnNext { count -> cachedSessionCount = count.toDouble() }
            .subscribe()

        Gauge.builder("sessions.active") { cachedSessionCount }
        .description("Current active sessions quantity")
        .register(registry)
    }

    private val reactiveSetOps = reactiveRedisTemplate.opsForSet()
    private val jwtTTL = Duration.ofMinutes(securityProperties.jwtTimeoutInMinutes)

    fun addSession(userId: UUID, sessionId: String): Mono<Void> {
        return reactiveSetOps.add("sessions:$userId", sessionId)
            .flatMap {
                reactiveRedisTemplate.expire(userId.toString(), jwtTTL)
            }
            .then()
    }

    fun removeSession(userId: UUID, sessionId: String): Mono<Void> {
        return reactiveSetOps.remove("sessions:$userId", sessionId)
            .then()
    }

    fun getAllSessions(userId: UUID): Flux<String> {
        return reactiveSetOps.members("sessions:$userId")
    }

    fun removeAllSessions(userId: UUID): Mono<Void> {
        return reactiveSetOps.delete("sessions:$userId")
            .then()
    }

    fun getCurrentSessionsQuantity(): Mono<Int> {
        return reactiveRedisTemplate.keys("sessions:*")
            .flatMap { key ->
                reactiveSetOps.size(key)
            }
            .collectList()
            .map { sizes -> sizes.sum().toInt() }
    }
}