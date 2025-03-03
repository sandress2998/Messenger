package ru.mephi.authentication.model.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.User

interface UserService {
    fun findByEmail(email: String): Mono<User>

    fun findById(id: Long): Mono<User>

    fun findAll(): Flux<User>

    fun create(email: String, hashedPassword: String): Mono<User>
}