package ru.mephi.authentication.controller.impl

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.mephi.authentication.annotation.TimeHttpRequest
import ru.mephi.authentication.config.TimerAspectConfig
import ru.mephi.authentication.controller.AuthorizationController
import ru.mephi.authentication.model.dto.request.RefreshRequest
import ru.mephi.authentication.model.dto.request.SigninRequest
import ru.mephi.authentication.model.dto.request.SignoutRequest
import ru.mephi.authentication.model.dto.request.SignupRequest
import ru.mephi.authentication.model.dto.response.*
import ru.mephi.authentication.model.service.SecurityService
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthorizationControllerImpl(
    private val securityService: SecurityService,
    private val timerAspectConfig: TimerAspectConfig,
    private val registry: MeterRegistry
): AuthorizationController {
    @PostMapping("/signin")
    @TimeHttpRequest("POST","/auth/signin")
    override fun signin(@RequestBody request: SigninRequest): Mono<SigninResponse> {
        signinCounter.increment()
        return securityService.signin(request)
            .doOnNext { response -> println("Signin response: $response") }
            .doOnError { error -> println("Signin error: ${error.message}") }
    }

    @PostMapping("/signup")
    @TimeHttpRequest("POST", "/auth/signup")
    override fun signup(@RequestBody request: SignupRequest): Mono<SignupResponse> {
        signupCounter.increment()
        return securityService.signup(request)
            .doOnNext { response -> println("Signup response: $response") }
            .doOnError { error -> println("Signup error: ${error.message}") }
    }

    @PostMapping("/refresh")
    @TimeHttpRequest("POST", "/auth/refresh")
    override fun refresh(@RequestBody request: RefreshRequest): Mono<RefreshResponse> {
        refreshCounter.increment()
        return securityService.refresh(request)
            .doOnNext { response -> println("Refresh response: $response") }
            .doOnError { error -> println("Refresh error: ${error.message}") }
    }

    @PostMapping("/signout")
    @TimeHttpRequest("POST", "/auth/signout")
    override fun signout(@RequestHeader("X-UserId") userId: String, @RequestBody request: SignoutRequest): Mono<SignoutResponse> {
        signoutCounter.increment()
        return securityService.signout(userId, request)
            .doOnNext { response -> println("Refresh response: $response") }
            .doOnError { error -> println("Refresh error: ${error.message}") }
    }

    @DeleteMapping("/invalidate_all")
    @TimeHttpRequest("DELETE", "/auth/invalidate_all")
    override fun invalidateAllTokens(@RequestHeader("X-UserId") userId: String): Mono<InvalidateAllResponse> {
        invalidateAllTokensCounter.increment()
        return securityService.invalidateAllTokens(userId)
            .doOnNext { response -> println("Response: $response") }
            .doOnError { error -> println("Error: ${error.message}") }
    }

    @DeleteMapping("/delete")
    @TimeHttpRequest("DELETE","/auth/delete")
    override fun deleteUser(@RequestHeader("X-UserId") userId: UUID): Mono<Void> {
        deleteUserCounter.increment()
        return securityService.deleteUser(userId)
    }

    private final val signinCounter: Counter
    private final val signupCounter: Counter
    private final val refreshCounter: Counter
    private final val signoutCounter: Counter
    private final val invalidateAllTokensCounter: Counter
    private final val deleteUserCounter: Counter

    init {
        // counter metrics init
        val metricName = "requests.total"

        signinCounter = Counter.builder(metricName)
            .tag("endpoint", "/auth/signin")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST","/auth/signin")
            }

        signupCounter = Counter.builder(metricName)
            .tag("endpoint", "/auth/signup")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST","/auth/signup")
            }

        refreshCounter = Counter.builder(metricName)
            .tag("endpoint", "/auth/refresh")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST", "/auth/refresh")
            }

        signoutCounter = Counter.builder(metricName)
            .tag("endpoint", "/auth/signout")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST","/auth/signout")
            }

        invalidateAllTokensCounter = Counter.builder(metricName)
            .tag("endpoint", "/auth/invalidate_all")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE","/auth/invalidate_all")
            }

        deleteUserCounter = Counter.builder(metricName)
            .tag("endpoint", "/auth/delete")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE", "/auth/delete")
            }
    }
}