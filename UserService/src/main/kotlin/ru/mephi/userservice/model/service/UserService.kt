package ru.mephi.userservice.model.service

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.userservice.database.entity.User
import ru.mephi.userservice.database.repository.UserRepository
import ru.mephi.userservice.model.UserAction
import ru.mephi.userservice.model.dto.CreateUserRequest
import ru.mephi.userservice.model.dto.UpdateUserRequest
import ru.mephi.userservice.model.dto.UserInfo
import ru.mephi.userservice.model.exception.BadRequestException
import ru.mephi.userservice.model.exception.NotFoundException
import ru.mephi.userservice.webclient.AuthService
import ru.mephi.userservice.webclient.PresenceService
import java.util.*


@Service
class UserService(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val presenceService: PresenceService,
    private val userNotificationService: UserNotificationService,
    private val userNotificationForChatMembersService: UserNotificationForChatMembersService
) {
    @Transactional
    @Timed(value = "business.operation.time",  description = "Time taken to execute business operations")
    fun createUser(user : CreateUserRequest): Mono<User> {
        val (userId, username, tag, email, showEmail) = user

        return userRepository.existsByTag(tag)
            .flatMap { exists ->
                if (!exists) {
                    userRepository.upsert(userId, username, tag, email, showEmail)
                } else {
                    Mono.error(BadRequestException("User with such tag already exists"))
                }
            }
    }

    @Timed(value = "business.operation.time",  description = "Time taken to execute business operations")
    fun updateUser(userId: UUID, request: UpdateUserRequest): Mono<User> {
        val (username, tag, email, showEmail) = request
        return userRepository.findUserById(userId)
            .switchIfEmpty {
                Mono.error(NotFoundException("User not found"))
            }
            .flatMap { oldUser ->
                userRepository.upsert(userId, username, tag, email, showEmail)
                    .then(Mono.defer {
                        val notifications = mutableListOf<Mono<Void>>()

                        // 1. Уведомление для чатов (если username изменился)
                        if (oldUser.username != username) {
                            notifications.add(
                                userNotificationForChatMembersService.notifyAboutUserAction(userId, UserAction.UPDATED)
                            )
                        }

                        // 2. Основное уведомление
                        notifications.add(
                            userNotificationService.notifyAboutUserAction(
                                userId,
                                UserAction.UPDATED,
                                UserInfo(username, tag, null, email, showEmail)
                            )
                        )

                        // Запускаем все уведомления и ждём их завершения
                        Mono.`when`(notifications)
                    })
            }
            .thenReturn(User(id = userId, username = username, tag = tag, email = email, showEmail = showEmail))
    }

    @Transactional
    @Timed(value = "business.operation.time",  description = "Time taken to execute business operations")
    fun deleteUser(userId : UUID): Mono<Void>{
        return userRepository.deleteUserById(userId)
            .then(authService.deleteUser(userId))
            .then(Mono.defer {
                val notifications = mutableListOf<Mono<Void>>()

                notifications.add(userNotificationService.notifyAboutUserAction(userId, UserAction.DELETED))
                notifications.add(userNotificationForChatMembersService.notifyAboutUserAction(userId, UserAction.DELETED))
                Mono.`when`(notifications)

            })
            .doOnError { e ->
                println("Failed to delete user in external service: ${e.message}")
            }
    }

    @Timed(value = "business.operation.time",  description = "Time taken to execute business operations")
    fun getUser(userId : UUID): Mono<UserInfo> {
        return userRepository.findUserById(userId)
            .flatMap { user ->
                presenceService.isUserActive(userId)
                .map { activityStatus ->
                    val (_, username, tag, email, showEmail) = user
                    if (showEmail) {
                        UserInfo(username, tag, activityStatus, email)
                    } else {
                        UserInfo(username, tag, activityStatus)
                    }
                }
            }
            .switchIfEmpty {
                Mono.error(NotFoundException("User wasn't found"))
            }
    }

    @Timed(value = "business.operation.time",  description = "Time taken to execute business operations")
    fun getCurrentUser(id: UUID): Mono<User> {
        return userRepository.findUserById(id)
            .switchIfEmpty(Mono.error(NotFoundException("User wasn't found")))
    }
}