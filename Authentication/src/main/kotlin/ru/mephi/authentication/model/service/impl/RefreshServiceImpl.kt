package ru.mephi.authentication.model.service.impl

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.dao.PasswordRepository
import ru.mephi.authentication.database.dao.RefreshRepository
import ru.mephi.authentication.database.entity.RefreshToken
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.RefreshService
import java.util.*

@Service
class RefreshServiceImpl(
    private val refreshRepository: RefreshRepository,
    private val passwordRepository: PasswordRepository
): RefreshService {
    val encoder = BCryptPasswordEncoder()

    // User пытается либо зарегистрироваться, либо войти
    @Transactional
    override fun generateToken(userId: String): Mono<String> {
        val token = UUID.randomUUID().toString()
        val hashedToken = encoder.encode(token)

        return passwordRepository.findByUserId(UUID.fromString(userId))
            .switchIfEmpty(Mono.error(UnauthorizedException("UserId not found"))) // Если пользователь не найден
            .flatMap { user ->
                refreshRepository.addToken(userId, hashedToken)
                    .flatMap { isAdded ->
                        if (isAdded) {
                            Mono.just(token)
                        } else {
                            Mono.error(UnauthorizedException("Failed to add token"))
                        }
                    }
            }
    }

    // User пытается по refresh-токен получить access-токен.
    override fun validateToken(userId: String, refreshToken: String): Mono<Boolean> {
        val tokensMono: Mono<List<RefreshToken>> = refreshRepository.getActiveTokens(userId)

        return tokensMono
            .flatMap { tokens ->
                if (tokens.isEmpty()) {
                    // Возвращаем ошибку, если токенов нет
                    Mono.error(UnauthorizedException("Token wasn't found"))
                } else {
                    // Проверяем, есть ли совпадение токенов
                    val isTokenValid = tokens.any { activeToken ->
                        encoder.matches(refreshToken, activeToken.hashedToken)
                    }
                    if (isTokenValid) {
                        Mono.just(true) // Токен валиден
                    } else {
                        Mono.just(false) // Токен невалиден
                    }
                }
            }
    }

    @Transactional
    override fun removeToken(userId: String, refreshToken: String): Mono<Boolean> {

        return refreshRepository.removeToken(userId, refreshToken)
            .map { quantityOfRemoved ->
                quantityOfRemoved >= 1
            }
    }

    @Transactional
    override fun updateToken(userId: String, refreshToken: String): Mono<String> {
        return removeToken(userId, refreshToken)
            .flatMap {
                generateToken(userId)
            }
    }

    @Transactional
    override fun removeAllTokens(userId: String): Mono<Boolean> {
        return refreshRepository.removeAllTokens(userId)
    }
}