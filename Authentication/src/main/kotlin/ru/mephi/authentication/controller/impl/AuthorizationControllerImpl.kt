package ru.mephi.authentication.controller.impl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.mephi.authentication.controller.AuthorizationController
import ru.mephi.authentication.dto.request.JwtTokenRequest
import ru.mephi.authentication.dto.request.SigninRequest
import ru.mephi.authentication.dto.request.SignoutRequest
import ru.mephi.authentication.dto.request.SignupRequest
import ru.mephi.authentication.dto.response.BaseResponse
import ru.mephi.authentication.model.service.SecurityService
import ru.mephi.authentication.property.SecurityProperties


@RestController
@RequestMapping("/auth")
class AuthorizationControllerImpl(
    private val securityService: SecurityService
): AuthorizationController {
    @PostMapping("/signin")
    override fun signin(@RequestBody request: SigninRequest): Mono<BaseResponse> {
        return securityService.signin(request)
            .doOnNext { response -> println("Signin response: $response") }
            .doOnError { error -> println("Signin error: ${error.message}") }
    }

    @PostMapping("/signup")
    override fun signup(@RequestBody request: SignupRequest): Mono<BaseResponse> {
        return securityService.signup(request)
            .doOnNext { response -> println("Signup response: $response") }
            .doOnError { error -> println("Signup error: ${error.message}") }
    }

    @PostMapping("/refresh")
    override fun refresh(@RequestBody request: JwtTokenRequest): Mono<BaseResponse> {
        return securityService.updateJwtToken(request)
            .doOnNext { response -> println("Refresh response: $response") }
            .doOnError { error -> println("Refresh error: ${error.message}") }
    }

    @PostMapping("/signout")
    override fun signout(@RequestBody request: SignoutRequest): Mono<BaseResponse> {
        return securityService.signout(request)
            .doOnNext { response -> println("Refresh response: $response") }
            .doOnError { error -> println("Refresh error: ${error.message}") }
    }
}