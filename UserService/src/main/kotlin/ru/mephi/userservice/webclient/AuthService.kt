package ru.mephi.userservice.webclient

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.*

@Component
class AuthService (
    private val authServiceWebClient: WebClient,
    private val registry: MeterRegistry
) {
    private final val webClientErrors: Counter = Counter.builder("webclient.error")
        .tag("webclient", "authentication-service")
        .register(registry)

    @Timed(
        value = "webclient.request",  description = "Time taken to send webclient requests",
        extraTags = ["request", "/auth/delete", "method", "DELETE"]
    )
    fun deleteUser(userId: UUID): Mono<Void> {
        return authServiceWebClient.delete()
            .uri("/auth/delete")
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { response ->
                webClientErrors.increment()
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
                webClientErrors.increment()
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