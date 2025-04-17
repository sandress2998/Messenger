package ru.mephi.presence.model.service

import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.rest.UserActivityResponse
import java.util.*


interface StatusService {
    fun setActive(userId: UUID): Mono<Void>

    fun setInactive(userId: UUID): Mono<Void>

    fun isActive(userId: UUID): Mono<UserActivityResponse>
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