package ru.mephi.chatservice.webclient

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Service
class MessageHandlerService(
    private val messageHandlerServiceWebClient: WebClient,
    private val registry: MeterRegistry
) {
    private final val webClientErrors: Counter = Counter.builder("webclient.error")
        .tag("webclient", "presence-service")
        .register(registry)

    @Timed(
        value = "webclient.request",  description = "Time taken to send a webclient request",
        extraTags = ["request", "/chats/{chatId}/users", "method", "POST"]
    )
    fun createMessageReadReceipt(userId: UUID, chatId: UUID): Mono<Void> {
        return messageHandlerServiceWebClient.post()
            .uri("/chats/{chatId}/users", chatId)
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(Void::class.java)
            .doOnError { e ->
                webClientErrors.increment()
                println("Error while creating message read receipt for user $userId in chat $chatId: ${e.message}")
            }
    }

    @Timed(
        value = "webclient.request",  description = "Time taken to send a webclient request",
        extraTags = ["request", "/chats/{chatId}/users", "method", "DELETE"]
    )
    fun deleteMessageReadReceipt(userId: UUID, chatId: UUID): Mono<Void> {
        return messageHandlerServiceWebClient.delete()
            .uri("/chats/{chatId}/users", chatId)
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(Void::class.java)
            .doOnError { e ->
                webClientErrors.increment()
                println("Error while deleting message read receipt for user $userId in chat $chatId: ${e.message}")
            }
    }

    @Timed(
        value = "webclient.request",  description = "Time taken to send a webclient request",
        extraTags = ["request", "/chats/{chatId}", "method", "DELETE"]
    )
    fun deleteChat(chatId: UUID): Mono<Void> {
        return messageHandlerServiceWebClient.delete()
            .uri("/chats/{chatId}", chatId)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(Void::class.java)
            .doOnError { e ->
                webClientErrors.increment()
                println("Error while deleting message read receipts for chat $chatId: ${e.message}")
            }
    }

    /* Больше не нужна
    private fun handleErrorResponse(response: ClientResponse): Mono<FailureResult> {
        return Mono.error(
            WebClientResponseException.create(
                response.statusCode().value(),  // Код статуса
                "Error: ${response.statusCode()}",  // Сообщение об ошибке
                response.headers().asHttpHeaders(),  // Заголовки ответа
                byteArrayOf(),  // Тело ответа (можно заменить на response.bodyToMono(ByteArray::class.java))
                null  // Кодировка (Charset)
            )
        )
    }
     */
}