package ru.mephi.authentication.model.service.impl

import io.micrometer.core.annotation.Timed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import ru.mephi.authentication.model.dto.request.RefreshRequest
import ru.mephi.authentication.model.dto.request.SigninRequest
import ru.mephi.authentication.model.dto.request.SignoutRequest
import ru.mephi.authentication.model.dto.request.SignupRequest
import ru.mephi.authentication.model.dto.response.*
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.PasswordService
import ru.mephi.authentication.model.service.SecurityService.Companion.CLASS_NAME
import ru.mephi.authentication.model.service.RefreshService
import ru.mephi.authentication.model.service.SecurityService
import ru.mephi.authentication.webclient.UserService
import ru.mephi.authentication.webclient.dto.CreateUserDTO
import java.util.*

@Service
class SecurityServiceImpl (
    private val jwtService: JwtService,
    private val passwordService: PasswordService,
    private val refreshService: RefreshService,
    private val userServiceWebClient: UserService
): SecurityService {
    val encoder = BCryptPasswordEncoder()
    private val log: Logger = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.signin"]  // пары ключ-значение
    )
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

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.signup"]  // пары ключ-значение
    )
    override fun signup(request: SignupRequest): Mono<SignupResponse> {
        val (username, tag, email, showEmail, password) = request

        log.info("Trying to register user with email: $email")

        return passwordService.findByEmail(email)
            .flatMap {
                log.info("User with email already exists: $email")
                Mono.error<SignupResponse>(UnauthorizedException("User with such an email already exists"))
            }
            .switchIfEmpty(
                passwordService.create(email, encoder.encode(password))
                .flatMap { user ->
                    userServiceWebClient.createUser(CreateUserDTO(user.id!!, username, tag, email, showEmail))
                    .then (Mono.defer {
                        println("User was successfully created")
                        val userId = user.id.toString()
                        Mono.zip(
                            refreshService.generateToken(userId),
                            Mono.just(jwtService.generateToken(userId)),
                        )
                    })
                    .onErrorResume { error ->
                        println("Error in UserService while user were creating...")
                        passwordService.removeByEmail(email)
                            .then(Mono.error(UnauthorizedException(error.message ?: "Unknown error")))
                    }
                }
                .onErrorResume { error ->
                    println("User wasn't created. An error has happened: ${error.message}")
                    Mono.error(UnauthorizedException(error.message ?: "Unknown error"))
                }
                .flatMap { tuple ->
                    val refreshToken = tuple.t1
                    val jwtToken = tuple.t2
                    Mono.just(SignupResponse(refreshToken, jwtToken))
                }
            )
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.refresh"]  // пары ключ-значение
    )
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
                        val jwtToken = jwtService.generateToken(userId)
                        Mono.just(RefreshResponse(updatedRefreshToken, jwtToken))
                    }
            }
            .switchIfEmpty(Mono.error(UnauthorizedException("User with such an email $email not found")))
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.signout"]  // пары ключ-значение
    )
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

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.invalidateAllTokens"]  // пары ключ-значение
    )
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

    @Transactional
    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.deleteUser"]  // пары ключ-значение
    )
    override fun deleteUser(userId: UUID): Mono<Void> {
        return passwordService.removeById(userId)
    }
}