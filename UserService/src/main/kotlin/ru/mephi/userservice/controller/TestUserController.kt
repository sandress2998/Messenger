package ru.mephi.userservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import ru.mephi.userservice.model.entity.User
import ru.mephi.userservice.service.UserService

@RestController
class TestUserController(private val userService: UserService) {
    @GetMapping("/users")
    fun getUsers(): Flux<User> {
        return userService.getUsers()
    }
}