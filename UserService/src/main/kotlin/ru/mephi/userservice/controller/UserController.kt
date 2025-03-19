package ru.mephi.userservice.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.mephi.userservice.model.dto.CreateUserDTO
import ru.mephi.userservice.model.dto.UpdateUserDTO
import ru.mephi.userservice.model.dto.UpdateUserHttpDTO
import ru.mephi.userservice.model.entity.User
import ru.mephi.userservice.service.UserService
import java.util.*

@RestController
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/users")
    fun addUser(@RequestBody user: CreateUserDTO) : Mono<User> {
        return userService.createUser(user)
    }
    @PatchMapping("/users/{userId}")
    fun updateUser(
        @RequestBody user: UpdateUserHttpDTO,
        @PathVariable userId: UUID
    ) : Mono<User> {
        return userService.updateUser(UpdateUserDTO(userId,user.username,user.email))
    }
    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable userId: UUID) : Mono<User> {
        return userService.getUser(userId)
    }
}

