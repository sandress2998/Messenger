package ru.mephi.authentication.database.dao

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password
import java.util.*

@Repository
interface PasswordRepository: ReactiveCrudRepository<Password, Long> {
    fun findByEmail(email: String): Mono<Password>

    fun findById(userId: UUID): Mono<Password>

    fun existsById(userId: UUID): Mono<Boolean>

    fun existsByEmail(email: String): Mono<Boolean>
}