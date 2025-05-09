package ru.mephi.chatservice.webclient

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.dto.rest.UserInfo
import java.util.*

@Service
class UserService (
    private val userServiceWebClient: WebClient,
    private val registry: MeterRegistry
) {
    private final val webClientErrors: Counter = Counter.builder("webclient.error")
        .tag("webclient", "user-service")
        .register(registry)

    @Timed(
        value = "webclient.request",  description = "Time taken to send a webclient request",
        extraTags = ["request", "/users/{userId}", "method", "GET"]
    )
    fun getUserInfo(userId: UUID): Mono<UserInfo> {
        return userServiceWebClient.get()
            .uri("/users/{userId}", userId)
            //.header("X-UserId", userId.toString())
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { response ->
                response.createException().flatMap { Mono.error(it) }
            }
            .bodyToMono(UserInfo::class.java)
            .doOnError { e ->
                webClientErrors.increment()
                println("Error while fetching user info for user $userId: ${e.message}")
            }
    }

}