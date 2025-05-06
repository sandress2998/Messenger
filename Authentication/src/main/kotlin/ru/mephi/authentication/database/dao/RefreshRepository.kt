package ru.mephi.authentication.database.dao

import io.micrometer.core.annotation.Timed
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.config.TimerAspectConfig
import ru.mephi.authentication.database.entity.RefreshToken
import ru.mephi.authentication.property.SecurityProperties
import java.time.Duration
import java.time.Instant

@Repository
class RefreshRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val securityProperties: SecurityProperties,
    private val timerAspectConfig: TimerAspectConfig
) {
    companion object {
        const val CLASS_NAME = "RefreshRepository"
    }

    val encoder = BCryptPasswordEncoder()

    // Добавить refresh-токен для пользователя
    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.addToken"]  // пары ключ-значение
    )
    fun addToken(userId: String, hashedToken: String): Mono<Boolean> {
        val expiresAt = Instant.now().plus(Duration.ofDays(7))
        val score = expiresAt.epochSecond.toDouble() // Преобразуем expiresAt в score
        return reactiveRedisTemplate.opsForZSet()
            .add("refresh:$userId", hashedToken, score) // Добавляем hashedToken с score
            .flatMap {
                // Устанавливаем TTL для ключа (например, 7 дней)
                reactiveRedisTemplate.expire("refresh:$userId", Duration.ofDays(securityProperties.refreshTimeoutInDays))
            }
    }

    // Получить все refresh-токены для пользователя
    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.getTokens"]  // пары ключ-значение
    )
    fun getTokens(userId: String): Mono<List<RefreshToken>> {
        val range = Range.unbounded<Long>()
        return reactiveRedisTemplate.opsForZSet()
            .rangeWithScores("refresh:$userId", range) // Получаем все элементы с score
            .collectList()
            .map { tuples ->
                tuples.map { tuple ->
                    RefreshToken(
                        hashedToken = tuple.value.toString() // Значение токена
                    )
                }
            }
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.getActiveTokens"]  // пары ключ-значение
    )
    fun getActiveTokens(userId: String): Mono<List<RefreshToken>> {
        val currentTime = Instant.now().epochSecond.toDouble()
        val range = Range.rightUnbounded(Range.Bound.exclusive(currentTime)) // Диапазон: (currentTime, +∞)
        return reactiveRedisTemplate.opsForZSet()
            .rangeByScoreWithScores("refresh:$userId", range) // Получаем активные токены с score
            .collectList()
            .map { tuples ->
                tuples.map { tuple ->
                    RefreshToken(
                        hashedToken = tuple.value.toString() // Значение токена
                    )
                }
            }
    }

    // Если пользователь явно выйдет из аккаунта
    @Timed(
        value = "db.query.time",  description = "Time taken to execute database queries",
        extraTags = ["type", "nosql", "operation", "$CLASS_NAME.removeToken"]  // пары ключ-значение
    )
    fun removeToken(userId: String, token: String): Mono<Long> {
        val range = Range.unbounded<Long>()
        return reactiveRedisTemplate.opsForZSet()
            .rangeWithScores("refresh:$userId", range)
            .filter { tuple ->
                val hashedToken = tuple.value.toString()
                encoder.matches(token, hashedToken)
            }
            .flatMap { tupleToRemove ->
                reactiveRedisTemplate.opsForZSet().remove("refresh:$userId", tupleToRemove.value.toString())
            }
            .collectList()
            .map { removedTokens -> removedTokens.size.toLong() }
    }

    // Удалить refresh-токен для пользователя
    @Timed(
        value = "db.query.time",  description = "Time taken to execute database queries",
        extraTags = ["type", "nosql", "operation", "$CLASS_NAME.removeExpiredTokens"]  // пары ключ-значение
    )
    fun removeExpiredTokens(userId: String): Mono<Long> {
        val currentTime = Instant.now().epochSecond.toDouble()
        val range = Range.leftUnbounded(Range.Bound.inclusive(currentTime)) // Диапазон: (-∞, currentTime]
        return reactiveRedisTemplate.opsForZSet()
            .removeRangeByScore("refresh:$userId", range) // Удаляем токены с score <= currentTime
    }

    // Удалить все токены для пользователя
    @Timed(
        value = "db.query.time",  description = "Time taken to execute database queries",
        extraTags = ["type", "nosql", "operation", "$CLASS_NAME.removeAllTokens"]  // пары ключ-значение
    )
    fun removeAllTokens(userId: String): Mono<Boolean> {
        return reactiveRedisTemplate.delete("refresh:$userId")
            .map { deletedCount -> deletedCount > 0 }
    }

    @Scheduled(fixedRate = 300000) // 300000 мс = 5 минут
    @Timed(
        value = "db.query.time",  description = "Time taken to execute database queries",
        extraTags = ["type", "nosql", "operation", "$CLASS_NAME.cleanupExpiredTokens"]  // пары ключ-значение
    )
    fun cleanupExpiredTokens() {
        // Удаляем истекшие токены для всех ключей
        reactiveRedisTemplate.keys("*") // Получаем все ключи
            .flatMap { key ->
                removeExpiredTokens(key)
            }
            .subscribe() // Запускаем выполнение
    }
}