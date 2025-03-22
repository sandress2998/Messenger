package ru.mephi.authentication.model.service

import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.RefreshRequest
import ru.mephi.authentication.dto.request.SignoutRequest

interface RefreshService {
    fun generateToken(userId: String): Mono<String>

    fun validateToken(userId: String, refreshToken: String): Mono<Boolean>

    fun removeToken(userId: String, refreshToken: String): Mono<Boolean>

    fun updateToken(userId: String, refreshToken: String): Mono<String>

    fun removeAllTokens(userId: String): Mono<Boolean>
}