package ru.mephi.authentication.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.database.dao.UserRepository
import ru.mephi.authentication.database.entity.User
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.UserService

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
): UserService {
    override fun findByEmail(email: String): Mono<User> {
        return userRepository.findByEmail(email)
    }

    override fun findById(id: Long): Mono<User> {
        return userRepository.findById(id)
    }

    override fun findAll(): Flux<User> {
        return userRepository.findAll()
    }

    override fun create(email: String, hashedPassword: String): Mono<User> {
        val user = User(email, hashedPassword)

        return userRepository.save(user)
            .flatMap { savedUser ->
                if (savedUser == null) {
                    Mono.error(UnauthorizedException("Unsuccessful user saving"))
                } else {
                    Mono.just(savedUser)
                }
            }
    }
}