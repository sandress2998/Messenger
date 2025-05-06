package ru.mephi.messagehandler.database.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.database.entity.MessageReadReceipt
import java.util.*

@Repository
interface MessageReadReceiptRepository: ReactiveMongoRepository<MessageReadReceipt, UUID> {
    fun getByUserIdAndChatId(userId: UUID, chatId: UUID): Mono<MessageReadReceipt>

    fun getByChatId(chatId: UUID): Flux<MessageReadReceipt>

    fun deleteByChatId(chatId: UUID): Mono<Void>

    fun deleteByUserIdAndChatId(userId: UUID, chatId: UUID): Mono<Void>
}