package ru.mephi.presence.model.service

import reactor.core.publisher.Mono

interface StatusService {
    fun setActive(email: String): Mono<Boolean>

    fun setInactive(email: String): Mono<Boolean>

    fun getStatus(email: String): Mono<String>

    fun setUserTracking(email: String, userWhoTracking: String): Mono<Boolean>

    fun setUserTracking(email: String, usersWhoTracking: Set<String>): Mono<Boolean>

    fun fetchUserTracking(email: String): Mono<List<String>>
}