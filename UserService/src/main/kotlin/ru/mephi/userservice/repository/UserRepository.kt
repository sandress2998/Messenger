package ru.mephi.userservice.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.userservice.model.entity.User
import java.util.*

@Repository
interface UserRepository : ReactiveCrudRepository<User, UUID> {
    fun findUserById(id : UUID) : Mono<User>
    fun deleteUserById(id : UUID): Mono<Void>
}