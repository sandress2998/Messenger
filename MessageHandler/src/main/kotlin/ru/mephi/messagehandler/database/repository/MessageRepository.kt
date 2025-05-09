package ru.mephi.messagehandler.database.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.database.entity.Message
import java.time.Instant
import java.util.*

@Repository
interface MessageRepository: ReactiveMongoRepository<Message, UUID> {
    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun findByChatIdAndTimestampBefore(
        chatId: UUID,
        timestamp: Instant,
        pageable: Pageable
    ): Flux<Message>

    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun findByChatIdAndTextContaining(
        chatId: UUID,
        text: String
    ): Flux<Message>

    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun findByChatIdAndTimestampAfter(
        chatId: UUID,
        timestamp: Instant
    ): Flux<Message>

    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun deleteMessageByChatId(chatId: UUID): Mono<Void>
}

/*
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
*/



