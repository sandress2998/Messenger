package ru.mephi.authentication.database.dao

import io.micrometer.core.annotation.Timed
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.entity.Password
import java.util.*


@Repository
interface PasswordRepository: ReactiveCrudRepository<Password, Long> {
    companion object {
        const val CLASS_NAME = "PasswordRepository"
    }

    @Timed(  // Делегируем стандартному @Timed
        value = "db.query.time", description = "Time taken to execute database queries",
        extraTags = ["type", "sql", "operation", "$CLASS_NAME.findByEmail"]
    )
    fun findByEmail(email: String): Mono<Password>

    @Timed(  // Делегируем стандартному @Timed
        value = "db.query.time", description = "Time taken to execute database queries",
        extraTags = ["type", "sql", "operation", "$CLASS_NAME.findById"]
    )
    fun findById(userId: UUID): Mono<Password>

    @Timed(  // Делегируем стандартному @Timed
        value = "db.query.time", description = "Time taken to execute database queries",
        extraTags = ["type", "sql", "operation", "$CLASS_NAME.existsById"]
    )
    fun existsById(userId: UUID): Mono<Boolean>

    @Timed(  // Делегируем стандартному @Timed
        value = "db.query.time", description = "Time taken to execute database queries",
        extraTags = ["type", "sql", "operation", "$CLASS_NAME.existsByEmail"]
    )
    fun existsByEmail(email: String): Mono<Boolean>

    @Timed(  // Делегируем стандартному @Timed
        value = "db.query.time", description = "Time taken to execute database queries",
        extraTags = ["type", "sql", "operation", "$CLASS_NAME.removeByEmail"]
    )
    fun removeByEmail(email: String): Mono<Boolean>

    @Timed(  // Делегируем стандартному @Timed
        value = "db.query.time", description = "Time taken to execute database queries",
        extraTags = ["type", "sql", "operation", "$CLASS_NAME.removeByEmail"]
    )
    fun removeById(id: UUID): Mono<Void>
}