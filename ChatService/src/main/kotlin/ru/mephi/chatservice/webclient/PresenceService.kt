package ru.mephi.chatservice.webclient

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.ActivityStatus
import ru.mephi.chatservice.model.dto.rest.UserActivityStatus
import java.util.*

@Service
class PresenceService (
    private val presenceServiceWebClient: WebClient,
    private val registry: MeterRegistry
) {
    private final val webClientErrors: Counter = Counter.builder("webclient.error")
        .tag("webclient", "presence-service")
        .register(registry)

    @Timed(
        value = "webclient.request",  description = "Time taken to send a webclient request",
        extraTags = ["request", "/activity", "method", "GET"]
    )
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
            .doOnError { e ->
                webClientErrors.increment()
                println("Error while fetching user activity status for user $userId: ${e.message}")
            }
    }
}