package ru.mephi.presence.controller

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.mephi.presence.annotation.TimeHttpRequest
import ru.mephi.presence.config.TimerAspectConfig
import ru.mephi.presence.model.dto.rest.UserActivityResponse
import ru.mephi.presence.model.service.StatusService
import java.util.*


@RestController
class ActivityController(
    private val statusService: StatusService,
    private val timerAspectConfig: TimerAspectConfig,
    private val registry: MeterRegistry
) {
    private final val isActiveCounter: Counter

    init {
        val metricName = "activity.requests.total"

        isActiveCounter = Counter.builder(metricName)
            .tag("endpoint", "/activity")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/activity")
            }
    }

    @GetMapping("/activity")
    @TimeHttpRequest("GET", "/activity")
    fun isActive(
        @RequestHeader("X-UserId") userId: UUID,
    ): Mono<UserActivityResponse> {
        isActiveCounter.increment()
        return statusService.isActive(userId)
    }
}


/*
@RestController
class ActivityController(
    private val statusService: StatusService
) {
    @GetMapping("/activity")
    fun isActive(
        @RequestHeader("X-UserId") userId: UUID,
    ): Mono<UserActivityResponse> {
        return statusService.isActive(userId)
    }
}
 */