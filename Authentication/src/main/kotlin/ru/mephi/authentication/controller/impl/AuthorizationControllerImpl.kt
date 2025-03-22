package ru.mephi.authentication.controller.impl

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.mephi.authentication.controller.AuthorizationController
import ru.mephi.authentication.dto.request.*
import ru.mephi.authentication.dto.response.*
import ru.mephi.authentication.model.service.SecurityService


@RestController
@RequestMapping("/auth")
class AuthorizationControllerImpl(
    private val securityService: SecurityService
): AuthorizationController {
    @PostMapping("/signin")
    override fun signin(@RequestBody request: SigninRequest): Mono<SigninResponse> {
        return securityService.signin(request)
            .doOnNext { response -> println("Signin response: $response") }
            .doOnError { error -> println("Signin error: ${error.message}") }
    }

    @PostMapping("/signup")
    override fun signup(@RequestBody request: SignupRequest): Mono<SignupResponse> {
        return securityService.signup(request)
            .doOnNext { response -> println("Signup response: $response") }
            .doOnError { error -> println("Signup error: ${error.message}") }
    }

    @PostMapping("/refresh")
    override fun refresh(@RequestBody request: RefreshRequest): Mono<RefreshResponse> {
        return securityService.refresh(request)
            .doOnNext { response -> println("Refresh response: $response") }
            .doOnError { error -> println("Refresh error: ${error.message}") }
    }

    @PostMapping("/signout")
    override fun signout(@RequestHeader("X-UserId") userId: String, @RequestBody request: SignoutRequest): Mono<SignoutResponse> {
        return securityService.signout(userId, request)
            .doOnNext { response -> println("Refresh response: $response") }
            .doOnError { error -> println("Refresh error: ${error.message}") }
    }

    @DeleteMapping("/invalidate_all")
    override fun invalidateAllTokens(@RequestHeader("X-UserId") userId: String): Mono<InvalidateAllResponse> {
        return securityService.invalidateAllTokens(userId)
            .doOnNext { response -> println("Response: $response") }
            .doOnError { error -> println("Error: ${error.message}") }
    }
}