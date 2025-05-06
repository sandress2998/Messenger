package ru.mephi.authentication.model.service.impl

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.dao.PasswordRepository
import ru.mephi.authentication.database.entity.Password
import ru.mephi.authentication.model.service.PasswordService
import ru.mephi.authentication.model.service.PasswordService.Companion.CLASS_NAME
import java.util.*

@Service
class PasswordServiceImpl(
    private val passwordRepository: PasswordRepository
): PasswordService {
    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.findByEmail"]  // пары ключ-значение
    )
    override fun findByEmail(email: String): Mono<Password> {
        return passwordRepository.findByEmail(email)
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.findById"]  // пары ключ-значение
    )
    override fun findById(id: UUID): Mono<Password> {
        return passwordRepository.findById(id)
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.findAll"]  // пары ключ-значение
    )
    override fun findAll(): Flux<Password> {
        return passwordRepository.findAll()
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.create"]  // пары ключ-значение
    )
    override fun create(email: String, hashedPassword: String): Mono<Password> {
        val newUser = Password(email, hashedPassword)
        return passwordRepository.save(newUser)
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.removeByEmail"]  // пары ключ-значение
    )
    override fun removeByEmail(email: String): Mono<Void> {
        return passwordRepository.removeByEmail(email)
            .then(Mono.empty())
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.removeById"]  // пары ключ-значение
    )
    override fun removeById(userId: UUID): Mono<Void> {
        return passwordRepository.removeById(userId)
    }
}