package ru.mephi.userservice.webclient

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.*

@Component
class AuthService (
    private val authServiceWebClient: WebClient
) {
    fun deleteUser(userId: UUID): Mono<Void> {
        return authServiceWebClient.delete()
            .uri("/auth/delete")
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { response ->
                return@onStatus Mono.error(
                    WebClientResponseException.create(
                        response.statusCode().value(), // Код статуса как Int
                        "Error: ${response.statusCode()}", // Сообщение об ошибке
                        response.headers().asHttpHeaders(), // Заголовки ответа
                        byteArrayOf(), // Тело ответа (ByteArray)
                        null // Кодировка (Charset)
                    )
                )
            }
            .onStatus({ httpStatusCode -> httpStatusCode.is5xxServerError }) { response ->
                return@onStatus Mono.error(
                    WebClientResponseException.create(
                        response.statusCode().value(), // Код статуса как Int
                        "Error: ${response.statusCode()}", // Сообщение об ошибке
                        response.headers().asHttpHeaders(), // Заголовки ответа
                        byteArrayOf(), // Тело ответа (ByteArray)
                        null // Кодировка (Charset)
                    )
                )
            }
            .bodyToMono()
    }
}