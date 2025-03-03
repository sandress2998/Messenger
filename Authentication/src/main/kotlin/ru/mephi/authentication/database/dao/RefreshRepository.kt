package ru.mephi.authentication.database.dao

import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.RefreshToken
import ru.mephi.authentication.property.SecurityProperties
import java.time.Duration
import java.time.Instant

@Repository
class RefreshRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val securityProperties: SecurityProperties
) {
    val encoder = BCryptPasswordEncoder()

    // Добавить refresh-токен для пользователя
    fun addToken(email: String, hashedToken: String): Mono<Boolean> {
        val expiresAt = Instant.now().plus(Duration.ofDays(7))
        val score = expiresAt.epochSecond.toDouble() // Преобразуем expiresAt в score
        return reactiveRedisTemplate.opsForZSet()
            .add(email, hashedToken, score) // Добавляем hashedToken с score
            .flatMap {
                // Устанавливаем TTL для ключа (например, 7 дней)
                reactiveRedisTemplate.expire(email, Duration.ofDays(securityProperties.refreshTimeoutInDays))
            }
    }

    // Получить все refresh-токены для пользователя
    fun getTokens(email: String): Mono<List<RefreshToken>> {
        val range = Range.unbounded<Long>()
        return reactiveRedisTemplate.opsForZSet()
            .rangeWithScores(email, range) // Получаем все элементы с score
            .collectList()
            .map { tuples ->
                tuples.map { tuple ->
                    RefreshToken(
                        hashedToken = tuple.value.toString() // Значение токена
                    )
                }
            }
    }

    fun getActiveTokens(email: String): Mono<List<RefreshToken>> {
        val currentTime = Instant.now().epochSecond.toDouble()
        val range = Range.rightUnbounded(Range.Bound.exclusive(currentTime)) // Диапазон: (currentTime, +∞)
        return reactiveRedisTemplate.opsForZSet()
            .rangeByScoreWithScores(email, range) // Получаем активные токены с score
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
    fun removeToken(email: String, token: String): Mono<Long> {
        val range = Range.unbounded<Long>()
        return reactiveRedisTemplate.opsForZSet()
            .rangeWithScores(email, range)
            .filter { tuple ->
                val hashedToken = tuple.value.toString()
                encoder.matches(token, hashedToken)
            }
            .flatMap { tupleToRemove ->
                reactiveRedisTemplate.opsForZSet().remove(email, tupleToRemove.value.toString())
            }
            .collectList()
            .map { removedTokens -> removedTokens.size.toLong() }
    }

    // Удалить refresh-токен для пользователя
    fun removeExpiredTokens(email: String): Mono<Long> {
        val currentTime = Instant.now().epochSecond.toDouble()
        val range = Range.leftUnbounded(Range.Bound.inclusive(currentTime)) // Диапазон: (-∞, currentTime]
        return reactiveRedisTemplate.opsForZSet()
            .removeRangeByScore(email, range) // Удаляем токены с score <= currentTime
    }

    // Удалить все токены для пользователя
    fun deleteTokens(email: String): Mono<Boolean> {
        return reactiveRedisTemplate.delete(email)
            .map { deletedCount -> deletedCount > 0 }
    }

    @Scheduled(fixedRate = 300000) // 300000 мс = 5 минут
    fun cleanupExpiredTokens() {
        val currentTime = Instant.now().epochSecond.toDouble()
        // Удаляем истекшие токены для всех ключей
        reactiveRedisTemplate.keys("*") // Получаем все ключи
            .flatMap { key ->
                removeExpiredTokens(key)
            }
            .subscribe() // Запускаем выполнение
    }
}