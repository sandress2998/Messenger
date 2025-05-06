package ru.mephi.chatservice.webclient

import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.dto.rest.UserInfo
import java.util.*

@Service
class UserService (
    private val userServiceWebClient: WebClient
) {
    fun getUserInfo(userId: UUID): Mono<UserInfo> {
        return userServiceWebClient.get()
            .uri("/users/{userId}", userId)
            //.header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(UserInfo::class.java)
            .onErrorResume { e ->
                println("Error while fetching user info for user $userId: ${e.message}")
                Mono.error(RuntimeException("Failed to fetch user info for user $userId: ${e.message}"))
            }
    }

}