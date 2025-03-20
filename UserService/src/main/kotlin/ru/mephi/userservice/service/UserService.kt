package ru.mephi.userservice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.userservice.model.dto.CreateUserDTO
import ru.mephi.userservice.model.dto.UpdateUserDTO
import ru.mephi.userservice.model.entity.User
import ru.mephi.userservice.repository.UserRepository
import java.util.UUID


@Service
class UserService(
    private val userRepository: UserRepository,
){
    @Transactional
    fun createUser(user : CreateUserDTO): Mono<User>{
        return userRepository.save(User(username = user.username , email = user.email))
    }

    @Transactional
    fun updateUser(user : UpdateUserDTO): Mono<User>{
        return userRepository.findUserById(user.id)
            .switchIfEmpty {
                Mono.error(Exception("User not found"))
            }.flatMap { userRepository.save(it.copy(username = user.username , email = user.email)) }
    }

    @Transactional
    fun deleteUser(userId : UUID): Mono<Void>{
        return userRepository.deleteUserById(userId)
    }

    fun getUser(id : UUID): Mono<User>{
        return userRepository.findUserById(id)
    }

    fun getUsers(): Flux<User> {
        return userRepository.findAll()
    }
}