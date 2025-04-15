package ru.mephi.presence.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.ChatActiveMembersResponse
import ru.mephi.presence.model.service.StatusService
import java.util.*

@RestController
class ActivityController(
    private val statusService: StatusService
) {
    @GetMapping("/chats/{chatId}/active")
    fun getActiveUsers(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<ChatActiveMembersResponse> {
        return statusService.fetchActiveMembers(userId, chatId)
    }
}