package ru.mephi.authentication.database.dao

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password
import java.util.*

@Repository
interface PasswordRepository: ReactiveCrudRepository<Password, Long> {
    fun findByEmail(email: String): Mono<Password>

    fun findByUserId(userId: UUID): Mono<Password>

    fun existsByEmail(email: String): Mono<Boolean>

    fun existsByUserId(userId: UUID): Mono<Boolean>

    //fun findUserById(id: Long): Mono<User>
}