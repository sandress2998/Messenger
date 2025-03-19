package ru.mephi.messagehandler.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.entity.Message
import java.util.*

interface MessageRepository : ReactiveMongoRepository<Message, UUID> {
    fun findMessageById(id: UUID): Mono<Message>
    fun existsMessageById(id: UUID): Mono<Boolean>

    fun findMessagesByChatId(chatId: UUID): Flux<Message>
    fun findMessagesBySenderId(userId: UUID): Flux<Message>
    fun findMessagesByChatIdAndSenderId(chatId: UUID, senderId: UUID): Flux<Message>

    fun deleteMessageById(id: UUID) : Mono<Void>
    fun deleteMessagesBySenderId(userId: UUID) : Mono<Void>
    fun deleteMessagesByChatId(chatId: UUID) : Mono<Void>
    fun deleteMessagesByChatIdAndSenderId(chatId: UUID, senderId: UUID) : Mono<Void>

}