package ru.mephi.messagehandler.database.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.database.entity.MessageReadReceipt
import java.util.*

@Repository
interface MessageReadReceiptRepository: ReactiveMongoRepository<MessageReadReceipt, UUID> {
    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun getByUserIdAndChatId(userId: UUID, chatId: UUID): Mono<MessageReadReceipt>

    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun getByChatId(chatId: UUID): Flux<MessageReadReceipt>

    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun deleteByChatId(chatId: UUID): Mono<Void>

    @Timed(
        value = "db.query.time", description = "Time taken to execute database queries"
    )
    fun deleteByUserIdAndChatId(userId: UUID, chatId: UUID): Mono<Void>
}