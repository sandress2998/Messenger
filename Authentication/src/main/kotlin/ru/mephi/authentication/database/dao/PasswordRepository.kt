package ru.mephi.authentication.database.dao

import io.micrometer.core.annotation.Timed
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password
import java.util.*


@Repository
interface PasswordRepository: ReactiveCrudRepository<Password, Long> {
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun findByEmail(email: String): Mono<Password>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun findById(userId: UUID): Mono<Password>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun existsById(userId: UUID): Mono<Boolean>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun existsByEmail(email: String): Mono<Boolean>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun removeByEmail(email: String): Mono<Boolean>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun removeById(id: UUID): Mono<Void>
}