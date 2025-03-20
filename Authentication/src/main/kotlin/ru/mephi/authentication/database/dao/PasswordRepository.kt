package ru.mephi.authentication.database.dao

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password

@Repository
interface PasswordRepository: ReactiveCrudRepository<Password, Long> {
    fun findByEmail(email: String): Mono<Password>

    //fun findUserById(id: Long): Mono<User>
}