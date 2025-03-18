package ru.mephi.presence.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.ChatActiveMembersRequest
import ru.mephi.presence.model.dto.ChatActiveMembersResponse
import ru.mephi.presence.model.service.StatusService

@RestController
class ActivityController(
    private val statusService: StatusService
) {
    @GetMapping("/chat/active")
    fun getActiveUsers(@RequestBody request: ChatActiveMembersRequest): Mono<ChatActiveMembersResponse> {
        return statusService.fetchActiveMembers(request)
    }
}