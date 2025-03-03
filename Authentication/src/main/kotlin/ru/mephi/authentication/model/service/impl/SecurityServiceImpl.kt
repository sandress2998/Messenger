package ru.mephi.authentication.model.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.JwtTokenRequest
import ru.mephi.authentication.dto.request.SigninRequest
import ru.mephi.authentication.dto.request.SignupRequest
import ru.mephi.authentication.dto.request.SignoutRequest
import ru.mephi.authentication.dto.response.BaseResponse
import ru.mephi.authentication.dto.response.SignoutResponse
import ru.mephi.authentication.dto.response.bad.BadResponse
import ru.mephi.authentication.dto.response.good.AuthGoodResponse
import ru.mephi.authentication.dto.response.good.JwtGoodResponse
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.RefreshService
import ru.mephi.authentication.model.service.SecurityService
import ru.mephi.authentication.model.service.UserService

@Service
class SecurityServiceImpl (
    private val jwtService: JwtService,
    private val userService: UserService,
    private val refreshService: RefreshService
): SecurityService {
    val encoder = BCryptPasswordEncoder()
    private val log: Logger = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    override fun signin(request: SigninRequest): Mono<BaseResponse> {
        val password = request.password
        val email = request.email

        return userService.findByEmail(email)
            .flatMap { user ->
                if (!encoder.matches(password, user.hashedPassword)) {
                    Mono.just(BadResponse(email, "Wrong password"))
                } else {
                    refreshService.generateToken(email)
                        .flatMap { refreshToken ->
                            val jwtToken = jwtService.generateToken(email)
                            Mono.just(AuthGoodResponse(email, refreshToken, jwtToken))
                        }
                }
            }
            .switchIfEmpty(Mono.just(BadResponse(email, "User with such an email $email not found")))

    }

    override fun signup(request: SignupRequest): Mono<BaseResponse> {
        val email = request.email
        val password = request.password

        log.info("Trying to register user with email: $email")

        return userService.findByEmail(email)
            .flatMap { user ->
                log.info("User with email already exists: $email")
                Mono.error<BaseResponse>(UnauthorizedException("User with such an email already exists"))
            }
            .switchIfEmpty(
                userService.create(email, encoder.encode(password))
                    .then(Mono.zip(
                        refreshService.generateToken(email),
                        Mono.just(jwtService.generateToken(email))
                    ))
                    .flatMap { tuple ->
                        val refreshToken = tuple.t1
                        val jwtToken = tuple.t2
                        Mono.just(AuthGoodResponse(email, refreshToken, jwtToken))
                    }
            )
    }

    override fun updateJwtToken(request: JwtTokenRequest): Mono<BaseResponse> {

        return refreshService.validateToken(request)
            .flatMap { isValid ->
                if (!isValid) {
                    Mono.error(UnauthorizedException("Token is invalid"))
                } else {
                    val jwtToken = jwtService.generateToken(request.email)
                    Mono.just(JwtGoodResponse(request.email, jwtToken))
                }
            }
    }

    override fun signout(request: SignoutRequest): Mono<BaseResponse> {

        return refreshService.removeToken(request)
            .map { isRemoved ->
                if (isRemoved) {
                    SignoutResponse(request.email, "Refresh token successfully removed")
                } else {
                    SignoutResponse(request.email, "Refresh token wasn't removed")
                }
            }
    }
}