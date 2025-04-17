package ru.mephi.presence.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.rest.UserActivityResponse
import ru.mephi.presence.model.service.StatusService
import java.util.*

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