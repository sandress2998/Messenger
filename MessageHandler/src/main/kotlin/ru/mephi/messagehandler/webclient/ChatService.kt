package ru.mephi.messagehandler.webclient

import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.exception.FailureResult
import ru.mephi.messagehandler.webclient.dto.ChatId
import ru.mephi.messagehandler.webclient.dto.UserDataInChat
import java.util.*

@Service
class ChatService (
    private val chatServiceWebClient: WebClient
) {
    fun getUserInChat(chatId: UUID, userId: UUID): Mono<UserDataInChat> {
        return chatServiceWebClient.get()
            .uri("/chats/{chatId}/users", chatId)
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { response ->
                handleErrorResponse(response)
            }
            .onStatus({ httpStatusCode -> httpStatusCode.is5xxServerError }) { response ->
                handleErrorResponse(response)
            }
            .bodyToMono(UserDataInChat::class.java)
    }

    fun getAllChatsForUser(userId: UUID): Flux<ChatId> {
        return chatServiceWebClient.get()
            .uri("/chats/id")
            .header("X-UserId", userId.toString())
            .accept(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM) // Для streaming ответов
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToFlux(ChatId::class.java)
            .onErrorResume { e ->
                println("Error fetching chats for user $userId: ${e.message}")
                Flux.error(RuntimeException("Failed to fetch chats"))
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