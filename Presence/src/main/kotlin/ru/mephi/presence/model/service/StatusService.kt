package ru.mephi.presence.model.service

import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.ChatActiveMembersRequest
import ru.mephi.presence.model.dto.ChatActiveMembersResponse


interface StatusService {
    fun connectToChat(email: String, chatID: String): Mono<Void>

    fun disconnectFromChat(email: String, chatID: String): Mono<Void>

    fun fetchActiveMembers(request: ChatActiveMembersRequest): Mono<ChatActiveMembersResponse>
}

/*
interface StatusService {
    fun setActive(email: String): Mono<Boolean>

    fun setInactive(email: String): Mono<Boolean>

    fun getStatus(email: String): Mono<String>

    fun setUserTracking(email: String, userWhoTracking: String): Mono<Boolean>

    fun setUserTracking(email: String, usersWhoTracking: Set<String>): Mono<Boolean>

    fun fetchUserTracking(email: String): Mono<List<String>>
}

 */