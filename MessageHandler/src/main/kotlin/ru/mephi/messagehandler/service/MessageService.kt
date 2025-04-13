package ru.mephi.messagehandler.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.MessageProperties
import ru.mephi.messagehandler.models.UserStatusInChat
import ru.mephi.messagehandler.models.dto.request.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.request.MessageSearchDTO
import ru.mephi.messagehandler.models.dto.request.MessageUpdateDTO
import ru.mephi.messagehandler.models.dto.response.RequestResult
import ru.mephi.messagehandler.models.dto.response.SuccessResult
import ru.mephi.messagehandler.models.dto.response.UnreadChanges
import ru.mephi.messagehandler.models.entity.Message
import ru.mephi.messagehandler.models.entity.MessageStatus
import ru.mephi.messagehandler.models.exception.AccessDeniedException
import ru.mephi.messagehandler.models.exception.FailureResult
import ru.mephi.messagehandler.models.exception.NotFoundException
import ru.mephi.messagehandler.repository.MessageRepository
import ru.mephi.messagehandler.util.UUIDUtil
import ru.mephi.messagehandler.webclient.ChatService
import ru.mephi.messagehandler.webclient.dto.UserDataInChat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList


@Service
class MessageService (
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: MessageRepository,
    private val chatService: ChatService,
    private val properties: MessageProperties,
    private val messageReadReceiptService: MessageReadReceiptService
) {
    fun getMessagesBefore (
        userId: UUID,
        chatId: UUID,
        startMessageId: UUID
    ): Flux<Message> {
        val pageable = PageRequest.of(
            0,
            properties.paginationMessagesQuantity,
            Sort.by(Sort.Direction.DESC, "timestamp")
        )
        return checkIfUserMember(userId, chatId)
            .then(messageRepository.findById(startMessageId))
            .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
            .flatMapMany { startMessage ->
                if (startMessage.chatId != chatId) {
                    Mono.error(NotFoundException("ChatId is wrong"))
                } else {
                    messageRepository.findByChatIdAndTimestampBefore(chatId, startMessage.timestamp, pageable)
                }
            }
    }

    // В MongoDB, как я поняла, делать транзакции себе дороже
    fun updateMessage (
        userId: UUID,
        chatId: UUID,
        messageId: UUID,
        updatedMessage: MessageUpdateDTO
    ): Mono<RequestResult> {
        val query = Query(Criteria.where("_id").`is`(messageId))
        val update = Update().set("text", updatedMessage.text)

        return checkIfUserMember(userId, chatId)
            .then(checkIfUserSender(userId, messageId))
            .then(mongoTemplate.findAndModify(query, update, Message::class.java))
            .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
            .then(messageReadReceiptService.addEditedMessage(chatId, messageId))
            .then(Mono.just(SuccessResult() as RequestResult))
    }

    // проблема с транзакциями
    fun deleteMessage (
        userId: UUID,
        chatId: UUID,
        messageId: UUID
    ): Mono<RequestResult> {
        return checkIfUserMember(userId, chatId)
            .then(checkIfUserSender(userId, messageId))
            .then(messageRepository.deleteById(messageId)) // сначала удаляем сообщение
            .then(messageReadReceiptService.addDeletedMessage(chatId, messageId)) // потом уведомляем об этом в message read receipt
            .thenReturn(SuccessResult() as RequestResult)
    }

    fun createMessage (
        userId: UUID,
        chatId: UUID,
        newMessage: MessageCreateDTO
    ): Mono<RequestResult> {
        return checkIfUserMember(userId, chatId)
            .then(messageRepository.save(Message(
                userId, chatId, newMessage.text, Instant.now(), UUID.randomUUID()
            )))
            .thenReturn(SuccessResult() as RequestResult)
    }

    fun searchMessages (
        userId: UUID,
        phraseToFind: MessageSearchDTO
    ): Flux<Message> {
        return chatService.getAllChatsForUser(userId)
            .flatMap { chatId ->
                messageRepository.findByChatIdAndTextContaining(
                    chatId, phraseToFind.phrase
                )
            }
    }

    fun markAsViewed(userId: UUID, chatId: UUID, messageId: UUID): Mono<RequestResult> {
        val binMessageId = UUIDUtil.toBinary(messageId)
        return mongoTemplate.findOne(
            Query(Criteria.where("_id").`is`(binMessageId)),
            Message::class.java
        ).flatMap { message ->
            if (message.senderId != userId) {
                Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_SENDER))
            } else if (message.viewedBy.contains(userId)) {
                Mono.just(SuccessResult())
            } else {
                val update = Update()
                    .addToSet("viewedBy", userId)
                    .set("status", MessageStatus.VIEWED)

                mongoTemplate.updateFirst(
                    Query(Criteria.where("_id").`is`(messageId)),
                    update,
                    Message::class.java
                ).flatMap { result ->
                    if (result.modifiedCount > 0) {
                        messageReadReceiptService.updateLastConfirmedTime(userId, chatId, message.timestamp)
                    } else {
                        Mono.empty()
                    }
                }.thenReturn(SuccessResult() as RequestResult)
            }
        }.switchIfEmpty(Mono.error(NotFoundException("Message not found")))
    }

    /*
    // лучше удалить
    // если админ хочет удалить
    // Не протестировано, но должно работать
    fun deleteAllMessages(
        userId: UUID,
        chatId: UUID
    ): Mono<RequestResult> {
        return checkIfUserAdmin(userId, chatId)
            .then(messageRepository.deleteMessageByChatId(chatId))
            .thenReturn(SuccessResult() as RequestResult)
    }
     */

    // Проблема с транзакциями
    // функция для использования другим микросервисом, там и происходит проверка на админа
    fun deleteChat(chatId: UUID): Mono<RequestResult> {
        return messageRepository.deleteMessageByChatId(chatId) // удаляем все сообщения
            .then(messageReadReceiptService.deleteChat(chatId)) // удаляем все message read receipt
            .thenReturn(SuccessResult())
    }

    fun getUnreadChanges(userId: UUID, chatId: UUID): Mono<UnreadChanges> {
        return checkIfUserMember(userId, chatId)
            .then(messageReadReceiptService.getByUserIdAndChatId(userId, chatId))
            .flatMap { messageReadReceipt ->
                val lastTimestamp = messageReadReceipt.lastConfirmedTime
                if (lastTimestamp == null) { // если прочитанных сообщений еще нет
                    val pageable = PageRequest.of(
                        0,
                        properties.paginationMessagesQuantity,
                        Sort.by(Sort.Direction.DESC, "timestamp")
                    )
                    messageRepository.findByChatIdAndTimestampBefore(chatId, Instant.now(), pageable)
                        .collectList()
                        .map { newMessages ->
                            val convertedNewMessages = ArrayList<Message>()
                            newMessages.forEach {
                                convertedNewMessages.add(it)
                            }
                            UnreadChanges(convertedNewMessages, ArrayList(), ArrayList())
                        }
                } else {
                    val newMessages = messageRepository.findByChatIdAndTimestampAfter(chatId, lastTimestamp).collectList()
                    val editedMessages = Flux.fromIterable(messageReadReceipt.pendingUpdates.edited)
                        .flatMap { messageId ->
                            messageRepository.findById(messageId)
                        }
                        .collectList()
                    val deletedId = messageReadReceipt.pendingUpdates.deleted
                    Mono.zip(newMessages, editedMessages)
                        .flatMap { tuple ->
                            val convertedNewMessages = ArrayList<Message>()
                            val convertedEditedMessages = ArrayList<Message>()
                            tuple.t1.forEach {
                                convertedNewMessages.add(it)
                            }
                            tuple.t2.forEach {
                                convertedEditedMessages.add(it)
                            }
                            Mono.just(UnreadChanges(convertedNewMessages, convertedEditedMessages, deletedId))
                        }
                }
            }
            .switchIfEmpty(Mono.error(NotFoundException("Message read receipt not found")))
    }

    private fun checkIfUserMember(userId: UUID, chatId: UUID): Mono<UserDataInChat> {
        return chatService.getUserInChat(chatId, userId)
            .flatMap { memberData ->
                if (memberData.role == UserStatusInChat.NOT_MEMBER) {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_MEMBER))
                } else {
                    Mono.just(memberData)
                }
            }
    }

    private fun checkIfUserAdmin(userId: UUID, chatId: UUID): Mono<UserDataInChat> {
        return chatService.getUserInChat(chatId, userId)
            .flatMap { memberData ->
                if (memberData.role == UserStatusInChat.ADMIN) {
                    Mono.just(memberData)
                } else {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                }
            }
    }

    private fun checkIfUserSender(userId: UUID, messageId: UUID): Mono<Void> {
        return messageRepository.findById(messageId)
            .flatMap { message ->
                if (message.senderId == userId) {
                    Mono.empty()
                } else {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_SENDER))
                }
            }
    }
}

/*
    fun getMessagesBefore (
        userId: UUID,
        chatId: UUID,
        startMessageId: UUID
    ): Flux<Message> {
        val pageable = PageRequest.of(
            0,
            properties.paginationMessagesQuantity,
            Sort.by(Sort.Direction.DESC, "timestamp")
        )
        return checkIfUserMember(userId, chatId)
            .then(messageRepository.findById(startMessageId))
            .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
            .flatMapMany { startMessage ->
                if (startMessage.chatId != chatId) {
                    Mono.error(NotFoundException("ChatId is wrong"))
                } else {
                    messageRepository.findByChatIdAndTimestampBefore(chatId, startMessage.timestamp, pageable)
                }
            }
    }
*/

/*
    fun updateMessageStatus(
        userId: UUID,
        chatId: UUID,
        messageId: UUID,
        status: MessageStatus
    ): Mono<RequestResult> {
        val binMessageId = UUIDUtil.toBinary(messageId)
        val query = Query(Criteria.where("_id").`is`(binMessageId))
        val update = Update().set("status", status)

        return checkIfUserMember(userId, chatId)
            .then(mongoTemplate.findAndModify(query, update, Message::class.java))
            .switchIfEmpty(Mono.error(Exception("Message not found")))
            .then(Mono.just(SuccessResult() as RequestResult))
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }
*/
/*
@Service
class MessageService(
    val messageRepository: MessageRepository,
) {
    private fun generateUniqueId(): Mono<UUID> {
        return Mono.defer {
            val id = UUID.randomUUID()
            messageRepository.existsById(id)
                .flatMap { exists ->
                    if (exists) {
                        // Если ID уже существует, рекурсивно генерируем новый
                        generateUniqueId()
                    } else {
                        // Если ID уникален, возвращаем его
                        Mono.just(id)
                    }
                }
        }
    }

    @Transactional
    fun createMessage(message: MessageCreateDTO): Mono<Message> {
        return generateUniqueId()
            .flatMap { id ->
                messageRepository.save(Message(id ,message.senderId,message.chatId,message.text,Date()))
            }
    }

    @Transactional
    fun updateMessage(message: MessageUpdateDTO): Mono<Message> {
        return messageRepository.findMessageById(id = message.id)
            .switchIfEmpty {
                Mono.error(NotFoundException("Message ${message.id} not found"))
            }.flatMap { messageRepository.save(it.copy(text = message.text)) }
    }

    fun getMessagesByChat(chatId: UUID): Flux<Message> {
        return messageRepository.findMessagesByChatId(chatId)
    }

    @Transactional
    fun deleteMessageById(messageId : UUID): Mono<Void> {
        return messageRepository.deleteMessageById(messageId)
    }

    @Transactional
    fun deleteMessagesByChatIdAndUserId(chatId : UUID,userId : UUID): Mono<Void> {
        return messageRepository.deleteMessagesByChatIdAndSenderId(chatId,userId)
    }

    @Transactional
    fun deleteMessageByChatId(chatId : UUID): Mono<Void> {
        return messageRepository.deleteMessagesByChatId(chatId)
    }
}
 */