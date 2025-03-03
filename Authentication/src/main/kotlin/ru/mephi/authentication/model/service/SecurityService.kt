package ru.mephi.authentication.model.service

import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.JwtTokenRequest
import ru.mephi.authentication.dto.request.SigninRequest
import ru.mephi.authentication.dto.request.SignupRequest
import ru.mephi.authentication.dto.request.SignoutRequest
import ru.mephi.authentication.dto.response.BaseResponse

interface SecurityService {
    fun signin(request: SigninRequest): Mono<BaseResponse>

    fun signup(request: SignupRequest): Mono<BaseResponse>

    fun updateJwtToken(request: JwtTokenRequest): Mono<BaseResponse>

    fun signout(request: SignoutRequest): Mono<BaseResponse>
}