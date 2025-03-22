package ru.mephi.authentication.model.service.impl

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.RefreshRequest
import ru.mephi.authentication.database.dao.RefreshRepository
import ru.mephi.authentication.database.dao.PasswordRepository
import ru.mephi.authentication.database.entity.RefreshToken
import ru.mephi.authentication.dto.request.SignoutRequest
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.RefreshService
import java.util.*

@Service
class RefreshServiceImpl(
    private val refreshRepository: RefreshRepository,
    private val passwordRepository: PasswordRepository,
    private val jwtService: JwtService
): RefreshService {
    val encoder = BCryptPasswordEncoder()

    // User пытается либо зарегистрироваться, либо войти
    @Transactional
    override fun generateToken(email: String): Mono<String> {
        val token = UUID.randomUUID().toString()
        val hashedToken = encoder.encode(token)

        return passwordRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(UnauthorizedException("Email not found"))) // Если пользователь не найден
            .flatMap { user ->
                refreshRepository.addToken(user.email, hashedToken)
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
    override fun validateToken(email: String, refreshToken: String): Mono<Boolean> {
        val tokensMono: Mono<List<RefreshToken>> = refreshRepository.getActiveTokens(email)

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
    override fun removeToken(email: String, refreshToken: String): Mono<Boolean> {

        return refreshRepository.removeToken(email, refreshToken)
            .map { quantityOfRemoved ->
                quantityOfRemoved >= 1
            }
    }

    @Transactional
    override fun updateToken(email: String, refreshToken: String): Mono<String> {
        return removeToken(email, refreshToken)
            .flatMap {
                generateToken(email)
            }
    }

    @Transactional
    override fun removeAllTokens(email: String): Mono<Boolean> {
        return refreshRepository.removeAllTokens(email)
    }
}