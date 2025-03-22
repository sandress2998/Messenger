package ru.mephi.authentication.model.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.authentication.dto.request.*
import ru.mephi.authentication.dto.response.*
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.RefreshService
import ru.mephi.authentication.model.service.SecurityService
import ru.mephi.authentication.model.service.PasswordService

@Service
class SecurityServiceImpl (
    private val jwtService: JwtService,
    private val passwordService: PasswordService,
    private val refreshService: RefreshService
): SecurityService {
    val encoder = BCryptPasswordEncoder()
    private val log: Logger = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    override fun signin(request: SigninRequest): Mono<SigninResponse> {
        val password = request.password
        val email = request.email

        return passwordService.findByEmail(email)
            .flatMap { user ->
                val userId = user.id.toString()
                if (!encoder.matches(password, user.hashedPassword)) {
                    Mono.error(UnauthorizedException("Wrong password"))
                } else {
                    refreshService.generateToken(userId)
                        .flatMap { refreshToken ->
                            val jwtToken = jwtService.generateToken(userId)
                            Mono.just<SigninResponse>(SigninResponse(refreshToken, jwtToken))
                        }
                }
            }
            .switchIfEmpty(Mono.error(UnauthorizedException("User with such an email $email not found")))
    }

    override fun signup(request: SignupRequest): Mono<SignupResponse> {
        val email = request.email
        val password = request.password

        log.info("Trying to register user with email: $email")

        return passwordService.findByEmail(email)
            .flatMap {
                log.info("User with email already exists: $email")
                Mono.error<SignupResponse>(UnauthorizedException("User with such an email already exists"))
            }
            .switchIfEmpty(
                passwordService.create(email, encoder.encode(password))
                .flatMap { user ->
                    val userId = user.id.toString()
                    Mono.zip(
                        refreshService.generateToken(userId),
                        Mono.just(jwtService.generateToken(userId))
                        )
                }
                .flatMap { tuple ->
                    val refreshToken = tuple.t1
                    val jwtToken = tuple.t2
                    Mono.just(SignupResponse(refreshToken, jwtToken))
                }
            )
    }

    override fun refresh(request: RefreshRequest): Mono<RefreshResponse> {
        val email: String = request.email
        val refreshToken = request.refreshToken

        return passwordService.findByEmail(email)
            .flatMap { user ->
                val userId = user.id.toString()
                refreshService.validateToken(userId, refreshToken)
                    .flatMap { isValid ->
                        if (!isValid) {
                            Mono.error(UnauthorizedException("Token is invalid"))
                        } else {
                            refreshService.updateToken(userId, refreshToken)
                        }
                    }
                    .flatMap { updatedRefreshToken ->
                        val jwtToken = jwtService.generateToken(email)
                        Mono.just(RefreshResponse(updatedRefreshToken, jwtToken))
                    }
            }
            .switchIfEmpty(Mono.error(UnauthorizedException("User with such an email $email not found")))
    }

    override fun signout(userId: String, request: SignoutRequest): Mono<SignoutResponse> {

        return refreshService.removeToken(userId, request.refresh)
            .map { isRemoved ->
                if (isRemoved) {
                    SignoutResponse("true")
                } else {
                    SignoutResponse("false")
                }
            }
    }

    override fun invalidateAllTokens(userId: String): Mono<InvalidateAllResponse> {

        return refreshService.removeAllTokens(userId)
            .map { isSucceed ->
            if (isSucceed) {
                InvalidateAllResponse("Tokens were deleted")
            } else {
                InvalidateAllResponse("No tokens were deleted")
            }
        }
    }
}