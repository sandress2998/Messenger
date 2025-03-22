package ru.mephi.authentication.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.dao.PasswordRepository
import ru.mephi.authentication.database.entity.Password
import ru.mephi.authentication.model.service.PasswordService
import java.util.*

@Service
class PasswordServiceImpl(
    private val passwordRepository: PasswordRepository
): PasswordService {
    override fun findByEmail(email: String): Mono<Password> {
        return passwordRepository.findByEmail(email)
    }

    override fun findById(id: UUID): Mono<Password> {
        return passwordRepository.findById(id)
    }

    override fun findAll(): Flux<Password> {
        return passwordRepository.findAll()
    }

    override fun create(email: String, hashedPassword: String): Mono<Password> {
        val newUser = Password(email, hashedPassword)
        return passwordRepository.save(newUser)
    }
}