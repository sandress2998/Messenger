package ru.mephi.authentication.controller

import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.*
import ru.mephi.authentication.dto.response.*
import java.util.*


interface AuthorizationController {
    fun signin(request: SigninRequest): Mono<SigninResponse>

    fun signup(request: SignupRequest) : Mono<SignupResponse>

    fun refresh(request: RefreshRequest): Mono<RefreshResponse>

    fun signout(userId: String, request: SignoutRequest): Mono<SignoutResponse>

    fun invalidateAllTokens(userId: String): Mono<InvalidateAllResponse>

    fun deleteUser(userId: UUID): Mono<Void>
}