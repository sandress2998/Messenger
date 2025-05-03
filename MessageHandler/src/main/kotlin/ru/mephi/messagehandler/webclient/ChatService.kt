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
import ru.mephi.messagehandler.models.exception.AccessDeniedException
import ru.mephi.messagehandler.models.responce.ErrorResponse
import ru.mephi.messagehandler.webclient.dto.ChatId
import ru.mephi.messagehandler.webclient.dto.MemberId
import ru.mephi.messagehandler.webclient.dto.UserId
import java.util.*

@Service
class ChatService (
    private val chatServiceWebClient: WebClient
) {
    fun getChatMemberInfo(chatId: UUID, userId: UUID): Mono<UUID> {
        return chatServiceWebClient.get()
            .uri("/chats/{chatId}/member", chatId)
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { response ->
                response.bodyToMono(ErrorResponse::class.java)
                    .flatMap { error ->
                        Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_MEMBER))
                    }
            }
            .onStatus({ httpStatusCode -> httpStatusCode.is5xxServerError }) { response ->
                handleErrorResponse(response)
            }
            .bodyToMono(MemberId::class.java)
            .map { it.memberId }
    }

    fun getAllChatsForUser(userId: UUID): Flux<UUID> {
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
            .map { chatId ->
                chatId.chatId
            }
            .onErrorResume { e ->
                println("Error fetching chats for user $userId: ${e.message}")
                Flux.error(RuntimeException("Failed to fetch chats"))
            }
    }

    fun getActiveUsersInChat(chatId: UUID): Flux<UUID> {
        return chatServiceWebClient.get()
            .uri("/chats/{chatId}/members/active", chatId)
            .accept(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM) // Для streaming ответов
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToFlux(UserId::class.java)
            .map { userId ->
                userId.userId
            }
            .onErrorResume { e ->
                println("Error fetching active user in chat $chatId: ${e.message}")
                Flux.error(RuntimeException("Failed to fetch active users in chats"))
            }
    }
/*
    fun getAllUsersInChat(chatId: UUID): Flux<UUID> {
        return chatServiceWebClient.get()
            .uri("/chats/{chatId}/users", chatId)
            .accept(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM) // Для streaming ответов
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToFlux(UserId::class.java)
            .map { userId ->
                userId.userId
            }
            .onErrorResume { e ->
                println("Error fetching users for chat $chatId: ${e.message}")
                Flux.error(RuntimeException("Failed to fetch users"))
            }
    }
*/
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