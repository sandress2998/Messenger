package ru.mephi.userservice.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.userservice.model.entity.User
import java.util.*

@Repository
interface UserRepository : ReactiveCrudRepository<User, UUID> {
    @Query("""
        INSERT INTO users (id, username, email)
        VALUES (:id, :username, :email)
        ON CONFLICT (id) DO UPDATE
        SET username = :username, email = :email
    """)
    fun upsert(
        @Param("id") id: UUID,
        @Param("username") username: String,
        @Param("email") email: String
    ): Mono<Void>

    fun findUserById(id : UUID) : Mono<User>

    fun deleteUserById(id : UUID): Mono<Void>
}