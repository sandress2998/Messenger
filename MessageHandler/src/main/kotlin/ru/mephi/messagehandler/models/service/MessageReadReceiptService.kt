package ru.mephi.messagehandler.models.service

import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.database.entity.MessageReadReceipt
import ru.mephi.messagehandler.models.exception.NotFoundException
import ru.mephi.messagehandler.database.repository.MessageReadReceiptRepository
import java.time.Instant
import java.util.*

@Service
class MessageReadReceiptService (
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageReadReceiptRepository : MessageReadReceiptRepository
) {
    fun deleteByChatId(chatId: UUID): Mono<Void> {
        return messageReadReceiptRepository.deleteByChatId(chatId)
    }

    fun getByUserIdAndChatId(userId: UUID, chatId: UUID): Mono<MessageReadReceipt> {
        return messageReadReceiptRepository.getByUserIdAndChatId(userId, chatId)
    }

    fun getByChatId(chatId: UUID): Flux<MessageReadReceipt> {
        return messageReadReceiptRepository.getByChatId(chatId)
    }

    fun updateLastConfirmedTime(userId: UUID, chatId: UUID, newTime: Instant): Mono<Void> {
        return getByUserIdAndChatId(userId, chatId)
            .flatMap { messageReadReceipt ->
                if (messageReadReceipt.lastConfirmedTime == null ||
                    messageReadReceipt.lastConfirmedTime.isBefore(newTime) ) {
                    val id = messageReadReceipt.id
                    val update = Update().set("lastConfirmedTime", newTime)
                    mongoTemplate.updateFirst(
                        Query(Criteria.where("_id").`is`(id)),
                        update,
                        MessageReadReceipt::class.java
                    )
                } else {
                    Mono.empty()
                }

            }
            .flatMap { result ->
                checkIfModified(result)
            }
            .then()
    }

    fun markEditedMessageAsProcessed(userId: UUID, chatId: UUID, editedMessageId: UUID): Mono<Void> {
        return getByUserIdAndChatId(userId, chatId)
            .flatMap { messageReadReceipt ->
                val id = messageReadReceipt.id
                val query = Query(Criteria.where("_id").`is`(id))
                val update = Update().pull("pendingUpdates.edited", editedMessageId)
                mongoTemplate.updateFirst(query, update, MessageReadReceipt::class.java)
            }
            .flatMap { result ->
                checkIfModified(result)
            }
    }

    fun markDeletedMessageAsProcessed(userId: UUID, chatId: UUID, deletedMessageId: UUID): Mono<Void> {
        return getByUserIdAndChatId(userId, chatId)
            .flatMap { messageReadReceipt ->
                val id = messageReadReceipt.id
                val query = Query(Criteria.where("_id").`is`(id))
                val update = Update().pull("pendingUpdates.deleted", deletedMessageId)
                mongoTemplate.updateFirst(query, update, MessageReadReceipt::class.java)
            }
            .flatMap { result ->
                checkIfModified(result)
            }
            .then()
    }

    fun addEditedMessage(chatId: UUID, editedMessageId: UUID): Mono<Void> {
        return getByChatId(chatId)
            .switchIfEmpty(Mono.error(NotFoundException("Not found messageReadReceipt for such chatId")))
            .flatMap { receipt ->
                val id = receipt.id
                if (receipt.pendingUpdates.edited.contains(editedMessageId)) {
                    Mono.empty()
                } else {
                    val update = Update()
                        .addToSet("pendingUpdates.edited", editedMessageId)
                    mongoTemplate.updateFirst(
                        Query(Criteria.where("_id").`is`(id)),
                        update,
                        MessageReadReceipt::class.java
                    )
                }
            }
            .then()
    }

    fun addDeletedMessage(chatId: UUID, deletedMessageId: UUID): Mono<Void> {
        return getByChatId(chatId)
            .switchIfEmpty(Mono.error(NotFoundException("Not found messageReadReceipt for such chatId")))
            .flatMap { receipt ->
                val id = receipt.id
                if (receipt.pendingUpdates.deleted.contains(deletedMessageId)) {
                    Mono.empty()
                } else {
                    val update = Update()
                        .addToSet("pendingUpdates.deleted", deletedMessageId)

                    mongoTemplate.updateFirst(
                        Query(Criteria.where("_id").`is`(id)),
                        update,
                        MessageReadReceipt::class.java
                    )
                }
            }
            .then()
    }

    fun create(userId: UUID, chatId: UUID): Mono<Void> {
        val messageReadReceipt = MessageReadReceipt(userId, chatId, UUID.randomUUID())
        return messageReadReceiptRepository.save(messageReadReceipt)
            .then()
    }

    fun delete(userId: UUID, chatId: UUID): Mono<Void> {
        return messageReadReceiptRepository.deleteByUserIdAndChatId(userId, chatId)
    }

    fun deleteChat(chatId: UUID): Mono<Void> {
        return messageReadReceiptRepository.deleteByChatId(chatId)
    }

    private fun checkIfModified(result: UpdateResult): Mono<Void> {
        return if (result.modifiedCount < 1) {
            Mono.error(NotFoundException("Something went wrong"))
        } else {
            Mono.empty()
        }
    }
}