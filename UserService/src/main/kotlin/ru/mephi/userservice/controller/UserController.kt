package ru.mephi.userservice.controller

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.mephi.userservice.annotation.TimeHttpRequest
import ru.mephi.userservice.config.TimerAspectConfig
import ru.mephi.userservice.model.dto.CreateUserRequest
import ru.mephi.userservice.model.dto.UserInfo
import ru.mephi.userservice.model.dto.UpdateUserRequest
import ru.mephi.userservice.database.entity.User
import ru.mephi.userservice.model.service.UserService
import java.util.*


@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val timerAspectConfig: TimerAspectConfig,
    private val registry: MeterRegistry
) {
    @PostMapping
    @TimeHttpRequest("POST", "/users")
    fun addUser(@RequestBody user: CreateUserRequest): Mono<User> {
        addUserCounter.increment()
        return userService.createUser(user)
    }

    @PutMapping
    @TimeHttpRequest("PUT", "/users")
    fun updateUser(
        @RequestBody request: UpdateUserRequest,
        @RequestHeader("X-UserId") userId: UUID
    ): Mono<User> {
        updateUserCounter.increment()
        return userService.updateUser(userId, request)
    }

    @GetMapping("/{userId}")
    @TimeHttpRequest("GET", "/users/{userId}")
    fun getUserById(@PathVariable userId: UUID): Mono<UserInfo> {
        getUserByIdCounter.increment()
        return userService.getUser(userId)
    }

    @DeleteMapping
    @TimeHttpRequest("DELETE", "/users")
    fun deleteUserById(@RequestHeader("X-UserId") userId: UUID): Mono<Void> {
        deleteUserByIdCounter.increment()
        return userService.deleteUser(userId)
    }

    @GetMapping("/me")
    @TimeHttpRequest("GET", "/users/me")
    fun getCurrentUserInfo(@RequestHeader("X-UserId") userId: UUID): Mono<User> {
        getCurrentUserInfoCounter.increment()
        return userService.getCurrentUser(userId)
    }

    private final val addUserCounter: Counter
    private final val updateUserCounter: Counter
    private final val getUserByIdCounter: Counter
    private final val deleteUserByIdCounter: Counter
    private final val getCurrentUserInfoCounter: Counter

    init {
        val metricName = "user.requests.total"

        addUserCounter = Counter.builder(metricName)
            .tag("endpoint", "/users")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST", "/users")
            }

        updateUserCounter = Counter.builder(metricName)
            .tag("endpoint", "/users")
            .tag("method", "PUT")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("PUT", "/users")
            }

        getUserByIdCounter = Counter.builder(metricName)
            .tag("endpoint", "/users/{userId}")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/users/{userId}")
            }

        deleteUserByIdCounter = Counter.builder(metricName)
            .tag("endpoint", "/users")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE", "/users")
            }

        getCurrentUserInfoCounter = Counter.builder(metricName)
            .tag("endpoint", "/users/me")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/users/me")
            }
    }
}


/*
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @PostMapping
    fun addUser(@RequestBody user: CreateUserRequest): Mono<User> {
        return userService.createUser(user)
    }

    @PutMapping
    fun updateUser(
        @RequestBody request: UpdateUserRequest,
        @RequestHeader("X-UserId") userId: UUID
    ): Mono<User> {
        return userService.updateUser(userId, request)
    }

    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: UUID): Mono<UserInfo> {
        return userService.getUser(userId)
    }

    @DeleteMapping
    fun deleteUserById(@RequestHeader("X-UserId") userId: UUID): Mono<Void> {
        return userService.deleteUser(userId)
    }

    @GetMapping("/me")
    fun getCurrentUserInfo(@RequestHeader("X-UserId") userId: UUID): Mono<User> {
        return userService.getCurrentUser(userId)
    }
}
*/
