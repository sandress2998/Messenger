package ru.mephi.userservice.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
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

