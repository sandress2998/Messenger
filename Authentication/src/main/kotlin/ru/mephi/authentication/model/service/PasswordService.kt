package ru.mephi.authentication.model.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password

interface PasswordService {
    fun findByEmail(email: String): Mono<Password>

    fun findById(id: Long): Mono<Password>

    fun findAll(): Flux<Password>

    fun create(email: String, hashedPassword: String): Mono<Password>
}