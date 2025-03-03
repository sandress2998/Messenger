package ru.mephi.authentication.database.dao

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.User

@Repository
interface UserRepository: ReactiveCrudRepository<User, Long> {
    fun findByEmail(email: String): Mono<User>

    //fun findUserById(id: Long): Mono<User>
}