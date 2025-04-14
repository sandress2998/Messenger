package ru.mephi.userservice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.userservice.model.dto.CreateUserDTO
import ru.mephi.userservice.model.dto.GetUserDTO
import ru.mephi.userservice.model.dto.UpdateUserDTO
import ru.mephi.userservice.model.entity.User
import ru.mephi.userservice.model.exception.FailureResponse
import ru.mephi.userservice.repository.UserRepository
import ru.mephi.userservice.webclient.AuthService
import java.util.UUID


@Service
class UserService(
    private val userRepository: UserRepository,
    private val authService: AuthService
) {
    @Transactional
    fun createUser(user : CreateUserDTO): Mono<User>{
        val userId = user.id
        val username = user.username
        val email = user.email

        return userRepository.upsert(userId, username, email)
            .thenReturn(User(id = userId, username = username , email = email))
    }

    @Transactional
    fun updateUser(userId: UUID, request: UpdateUserDTO): Mono<User>{
        val username = request.username
        val email = request.email
        return userRepository.findUserById(userId)
            .switchIfEmpty {
                Mono.error(Exception("User not found"))
            }
            .flatMap {
                userRepository.upsert(userId, username, email)
            }
            .thenReturn(User(id = userId, username = username , email = email))
    }

    @Transactional
    fun deleteUser(userId : UUID): Mono<Void>{
        return userRepository.deleteUserById(userId)
            .then(authService.deleteUser(userId))
            .onErrorMap { e ->
                // Преобразование ошибки в RuntimeException или в любое другое исключение,
                // которое приведет к откату транзакции.
                RuntimeException("Failed to delete user in external service", e)
            }
    }

    fun getUser(id : UUID): Mono<GetUserDTO> {
        return userRepository.findUserById(id)
            .map { user ->
                GetUserDTO(user.username, user.email)
            }
            .switchIfEmpty {
                Mono.error(FailureResponse("User wasn't found"))
            }
    }

    fun getUsers(): Flux<User> {
        return userRepository.findAll()
    }
}