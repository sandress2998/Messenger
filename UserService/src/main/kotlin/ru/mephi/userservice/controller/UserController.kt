package ru.mephi.userservice.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.mephi.userservice.model.dto.CreateUserDTO
import ru.mephi.userservice.model.dto.GetUserDTO
import ru.mephi.userservice.model.dto.UpdateUserDTO
import ru.mephi.userservice.model.entity.User
import ru.mephi.userservice.service.UserService
import java.util.*

@RestController
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/users")
    fun addUser(@RequestBody user: CreateUserDTO): Mono<User> {
        return userService.createUser(user)
    }

    @PatchMapping("/users")
    fun updateUser(
        @RequestBody request: UpdateUserDTO,
        @RequestHeader("X-UserId") userId: UUID
    ): Mono<User> {
        return userService.updateUser(userId, request)
    }

    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable userId: UUID): Mono<GetUserDTO> {
        return userService.getUser(userId)
    }

    @DeleteMapping("/users")
    fun deleteUserById(@RequestHeader("X-UserId") userId: UUID): Mono<Void> {
        return userService.deleteUser(userId)
    }
}

