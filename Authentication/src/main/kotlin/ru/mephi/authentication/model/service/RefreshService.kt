package ru.mephi.authentication.model.service

import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.JwtTokenRequest
import ru.mephi.authentication.dto.request.SignoutRequest

interface RefreshService {
    fun generateToken(email: String): Mono<String>

    fun validateToken(request: JwtTokenRequest): Mono<Boolean>

    fun removeToken(request: SignoutRequest): Mono<Boolean>
}