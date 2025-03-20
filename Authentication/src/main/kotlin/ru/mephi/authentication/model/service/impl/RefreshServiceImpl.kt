package ru.mephi.authentication.model.service.impl

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.JwtTokenRequest
import ru.mephi.authentication.database.dao.RefreshRepository
import ru.mephi.authentication.database.dao.PasswordRepository
import ru.mephi.authentication.database.entity.RefreshToken
import ru.mephi.authentication.dto.request.SignoutRequest
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
    override fun validateToken(request: JwtTokenRequest): Mono<Boolean> {
        val refreshToken: RefreshToken = RefreshToken(request.refreshToken)
        val tokensMono: Mono<List<RefreshToken>> = refreshRepository.getActiveTokens(request.email)

        return tokensMono
            .flatMap { tokens ->
                if (tokens.isEmpty()) {
                    // Возвращаем ошибку, если токенов нет
                    Mono.error<Boolean>(UnauthorizedException("Token wasn't found"))
                } else {
                    // Проверяем, есть ли совпадение токенов
                    val isTokenValid = tokens.any { activeToken ->
                        encoder.matches(refreshToken.hashedToken, activeToken.hashedToken)
                    }
                    if (isTokenValid) {
                        Mono.just(true) // Токен валиден
                    } else {
                        Mono.just(false) // Токен невалиден
                    }
                }
            }
    }

    override fun removeToken(request: SignoutRequest): Mono<Boolean> {
        val email = request.email
        val refreshToken = request.refreshToken

        return refreshRepository.removeToken(email, refreshToken)
            .map { quantityOfRemoved ->
                quantityOfRemoved >= 1
            }
    }
}