package ru.mephi.chatservice.webclient

import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.exception.FailureResult
import java.util.*

@Service
class MessageHandlerService(
    private val messageHandlerServiceWebClient: WebClient
) {
    fun createMessageReadReceipt(userId: UUID, chatId: UUID): Mono<Void> {
        return messageHandlerServiceWebClient.post()
            .uri("/chats/{chatId}/users", chatId)
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(Void::class.java)
            .onErrorResume { e ->
                println("Error while creating message read receipt for user $userId in chat $chatId: ${e.message}")
                Mono.error(RuntimeException("Failed to create message read receipt for user $userId in chat $chatId: ${e.message}"))
            }
    }

    fun deleteMessageReadReceipt(userId: UUID, chatId: UUID): Mono<Void> {
        return messageHandlerServiceWebClient.delete()
            .uri("/chats/{chatId}/users", chatId)
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(Void::class.java)
            .onErrorResume { e ->
                println("Error while deleting message read receipt for user $userId in chat $chatId: ${e.message}")
                Mono.error(RuntimeException("Failed to delete message read receipt for user $userId in chat $chatId: ${e.message}"))
            }
    }

    fun deleteChat(chatId: UUID): Mono<Void> {
        return messageHandlerServiceWebClient.delete()
            .uri("/chats/{chatId}", chatId)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(Void::class.java)
            .onErrorResume { e ->
                println("Error while deleting message read receipts for chat $chatId: ${e.message}")
                Mono.error(RuntimeException("Failed to delete message read receipts for chat $chatId"))
            }
    }

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
}