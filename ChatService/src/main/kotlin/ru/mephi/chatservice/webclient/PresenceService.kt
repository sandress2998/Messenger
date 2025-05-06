package ru.mephi.chatservice.webclient

import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.ActivityStatus
import ru.mephi.chatservice.model.dto.rest.UserActivityStatus
import java.util.UUID

@Service
class PresenceService (
    private val presenceServiceWebClient: WebClient
) {
    fun isUserActive(userId: UUID): Mono<ActivityStatus> {
        return presenceServiceWebClient.get()
            .uri("/activity")
            .header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(UserActivityStatus::class.java)
            .map { it.status }
            .onErrorResume { e ->
                println("Error while fetching user activity status for user $userId: ${e.message}")
                Mono.error(RuntimeException("Failed to fetch user activity status for user $userId: ${e.message}"))
            }
    }
}