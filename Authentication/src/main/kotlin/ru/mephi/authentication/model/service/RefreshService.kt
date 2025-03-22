package ru.mephi.authentication.model.service

import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.RefreshRequest
import ru.mephi.authentication.dto.request.SignoutRequest

interface RefreshService {
    fun generateToken(email: String): Mono<String>

    fun validateToken(email: String, refreshToken: String): Mono<Boolean>

    fun removeToken(email: String, refreshToken: String): Mono<Boolean>

    fun updateToken(email: String, refreshToken: String): Mono<String>

    fun removeAllTokens(email: String): Mono<Boolean>
}