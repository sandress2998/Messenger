package ru.mephi.authentication.controller

import reactor.core.publisher.Mono
import ru.mephi.authentication.model.dto.request.RefreshRequest
import ru.mephi.authentication.model.dto.request.SigninRequest
import ru.mephi.authentication.model.dto.request.SignoutRequest
import ru.mephi.authentication.model.dto.request.SignupRequest
import ru.mephi.authentication.model.dto.response.*
import java.util.*


interface AuthorizationController {
    fun signin(request: SigninRequest): Mono<SigninResponse>

    fun signup(request: SignupRequest) : Mono<SignupResponse>

    fun refresh(request: RefreshRequest): Mono<RefreshResponse>

    fun signout(userId: String, request: SignoutRequest): Mono<SignoutResponse>

    fun invalidateAllTokens(userId: String): Mono<InvalidateAllResponse>

    fun deleteUser(userId: UUID): Mono<Void>
}