package ru.mephi.authentication.controller

import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.*
import ru.mephi.authentication.dto.response.*


interface AuthorizationController {
    fun signin(request: SigninRequest): Mono<SigninResponse>

    fun signup(request: SignupRequest) : Mono<SignupResponse>

    fun refresh(request: RefreshRequest): Mono<RefreshResponse>

    fun signout(email: String, request: SignoutRequest): Mono<SignoutResponse>

    fun invalidateAllTokens(email: String): Mono<InvalidateAllResponse>
}