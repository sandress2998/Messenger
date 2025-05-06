package ru.mephi.userservice.database.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.userservice.database.entity.User
import java.util.*

@Repository
interface UserRepository : ReactiveCrudRepository<User, UUID> {
    @Query("""
        INSERT INTO users (id, username, tag, email, show_email)
        VALUES (:id, :username, :tag, :email, :show_email)
        ON CONFLICT (id) DO UPDATE
        SET username = :username, tag = :tag, email = :email, show_email = :show_email
    """)
    fun upsert(
        @Param("id") userId: UUID,
        @Param("username") username: String,
        @Param("tag") tag: String,
        @Param("email") email: String,
        @Param("show_email") showEmail: Boolean
    ): Mono<Void>

    fun findUserById(id : UUID) : Mono<User>

    fun deleteUserById(id : UUID): Mono<Void>
}