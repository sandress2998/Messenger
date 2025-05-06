package ru.mephi.authentication.model.service

import reactor.core.publisher.Mono

interface RefreshService {
    companion object {
        const val CLASS_NAME = "RefreshService"
    }

    fun generateToken(userId: String): Mono<String>

    fun validateToken(userId: String, refreshToken: String): Mono<Boolean>

    fun removeToken(userId: String, refreshToken: String): Mono<Boolean>

    fun updateToken(userId: String, refreshToken: String): Mono<String>

    fun removeAllTokens(userId: String): Mono<Boolean>
}