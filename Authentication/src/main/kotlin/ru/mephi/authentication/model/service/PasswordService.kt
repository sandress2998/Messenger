package ru.mephi.authentication.model.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password
import java.util.*

interface PasswordService {
    companion object {
        const val CLASS_NAME = "PasswordService"
    }

    fun findByEmail(email: String): Mono<Password>

    fun findById(id: UUID): Mono<Password>

    fun findAll(): Flux<Password>

    fun create(email: String, hashedPassword: String): Mono<Password>

    fun removeByEmail(email: String): Mono<Void>

    fun removeById(userId: UUID): Mono<Void>
}