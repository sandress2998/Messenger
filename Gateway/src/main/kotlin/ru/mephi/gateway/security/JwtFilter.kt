package ru.mephi.gateway.security

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtFilter(
    private val jwtService: JwtService
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response

        // Пропускаем публичные маршруты
        if (request.uri.path.startsWith("/auth/")) {
            return chain.filter(exchange)
        }

        // Извлекаем токен из заголовка Authorization
        val token = request.headers.getFirst("Authorization")?.substringAfter("Bearer ")

        if (token == null) {
            return writeErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Token is missing"
            )
        }

        try {
            // Проверяем токен
            val claims = jwtService.validateToken(token)
            val username = claims.subject

            // Создаем объект Authentication
            val authorities = Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")) // Роли пользователя
            val authentication: Authentication = UsernamePasswordAuthenticationToken(username, null, authorities)

            // Устанавливаем аутентификацию в SecurityContext
            val securityContext = SecurityContextImpl(authentication)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        } catch (ex: RuntimeException) {
            return writeErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid token: ${ex.message}"
            )
        } catch (ex: Exception) {
            return writeErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An error occurred during token validation: ${ex.message}"
            )
        }
    }

    private fun writeErrorResponse(
        response: ServerHttpResponse,
        status: HttpStatus,
        error: String,
        message: String
    ): Mono<Void> {
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        val json = """
            {
                "status": ${status.value()},
                "error": "$error",
                "message": "$message"
            }
        """.trimIndent()

        val bufferFactory = DefaultDataBufferFactory()
        val dataBuffer: DataBuffer = bufferFactory.wrap(json.toByteArray(StandardCharsets.UTF_8))

        return response.writeWith(Mono.just(dataBuffer))
    }
}