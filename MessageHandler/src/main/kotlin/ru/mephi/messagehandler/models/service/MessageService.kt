package ru.mephi.messagehandler.models.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.MessageAction
import ru.mephi.messagehandler.models.MessageProperties
import ru.mephi.messagehandler.models.dto.kafka.NewMessageInfo
import ru.mephi.messagehandler.models.dto.kafka.UpdatedMessageInfo
import ru.mephi.messagehandler.models.dto.rest.request.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.rest.request.MessageSearchDTO
import ru.mephi.messagehandler.models.dto.rest.request.MessageUpdateDTO
import ru.mephi.messagehandler.models.dto.rest.response.RequestResult
import ru.mephi.messagehandler.models.dto.rest.response.SuccessResult
import ru.mephi.messagehandler.models.dto.rest.response.UnreadChanges
import ru.mephi.messagehandler.database.entity.Message
import ru.mephi.messagehandler.database.entity.MessageStatus
import ru.mephi.messagehandler.models.exception.AccessDeniedException
import ru.mephi.messagehandler.models.exception.NotFoundException
import ru.mephi.messagehandler.database.repository.MessageRepository
import ru.mephi.messagehandler.util.UUIDUtil
import ru.mephi.messagehandler.webclient.ChatService
import java.time.Instant
import java.util.*


@Service
class MessageService (
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: MessageRepository,
    private val chatService: ChatService,
    private val properties: MessageProperties,
    private val messageReadReceiptService: MessageReadReceiptService,
    private val messageNotificationService: MessageNotificationService
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
            .flatMap { memberId ->
                checkIfMemberSender(memberId, messageId)
                .then(mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Message::class.java))
                    .flatMap { message ->
                        messageReadReceiptService.addEditedMessage(chatId, messageId)
                        .then(messageNotificationService.notifyChatMembersAboutMessageAction(
                            memberId, chatId, messageId, MessageAction.UPDATED, UpdatedMessageInfo.messageAsNotification(message))
                        )
                        .thenReturn(SuccessResult())
                    }
                .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
            }
    }

    // проблема с транзакциями
    fun deleteMessage (
        userId: UUID,
        chatId: UUID,
        messageId: UUID
    ): Mono<RequestResult> {
        return checkIfUserMember(userId, chatId)
            .flatMap { memberId ->
                checkIfMemberSender(memberId, messageId)
                .then(messageRepository.deleteById(messageId)) // сначала удаляем сообщение
                .then(messageReadReceiptService.addDeletedMessage(chatId, messageId)) // потом уведомляем об этом в message read receipt
                .then(messageNotificationService.notifyChatMembersAboutMessageAction(memberId, chatId, memberId, MessageAction.DELETED))
            }
            .thenReturn(SuccessResult() as RequestResult)
    }

    fun createMessage (
        userId: UUID,
        chatId: UUID,
        newMessage: MessageCreateDTO
    ): Mono<RequestResult> {
        return checkIfUserMember(userId, chatId)
            .flatMap { memberId ->
                messageRepository.save(
                    Message(
                    memberId, chatId, newMessage.text, Instant.now(), UUID.randomUUID()
                )
                )
                .flatMap { message ->
                    messageNotificationService.notifyChatMembersAboutMessageAction(
                        memberId, chatId, message.id, MessageAction.NEW, NewMessageInfo.messageAsNotification(message)
                    )
                }
            }
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
                val editedMessagesId = messageReadReceipt.pendingUpdates.edited
                val deletedMessagesId = messageReadReceipt.pendingUpdates.deleted
                if (lastTimestamp == null) { // если прочитанных сообщений еще нет
                    val pageable = PageRequest.of(
                        0,
                        properties.paginationMessagesQuantity,
                        Sort.by(Sort.Direction.DESC, "timestamp")
                    )
                    messageRepository.findByChatIdAndTimestampBefore(chatId, Instant.now(), pageable)
                        .collectList()
                        .flatMap { newMessages ->
                            val convertedNewMessages = ArrayList<Message>()
                            newMessages.forEach {
                                convertedNewMessages.add(it)
                            }
                            Flux.fromIterable(editedMessagesId)
                                .flatMap { messageId ->
                                    messageRepository.findById(messageId)
                                }
                                .collectList()
                                .map { editedMessages ->
                                    UnreadChanges(convertedNewMessages, editedMessages, deletedMessagesId)
                                }
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

    fun markAsHandledMessage(userId: UUID, chatId: UUID, messageId: UUID, action: MessageAction): Mono<RequestResult> {
        return checkIfUserMember(userId, chatId)
            .flatMap { memberId ->
                messageRepository.findById(messageId)
                    .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
                    .flatMap { message ->
                        when (action) {
                            MessageAction.VIEWED -> {
                                //messageReadReceiptService.updateLastConfirmedTime(userId, chatId, message.timestamp)
                                markAsViewed(memberId, chatId, messageId)
                            }
                            MessageAction.UPDATED -> {
                                messageReadReceiptService.markEditedMessageAsProcessed(userId, chatId, messageId)
                            }
                            MessageAction.DELETED -> {
                                messageReadReceiptService.markDeletedMessageAsProcessed(userId,chatId, messageId)
                            }
                            // ВОТ ЗДЕСЬ УЖЕ НАЧИНАЮТСЯ ИЗМЕНЕНИЯ
                            MessageAction.NEW -> {
                                messageReadReceiptService.updateLastConfirmedTime(userId,chatId, message.timestamp) }
                            }
                    }
            }
            .thenReturn(SuccessResult())
    }

    private fun markAsViewed(memberId: UUID, chatId: UUID, messageId: UUID): Mono<Void> {
        val binMessageId = UUIDUtil.toBinary(messageId)
        return mongoTemplate.findOne(
            Query(Criteria.where("_id").`is`(binMessageId)),
            Message::class.java
        )
            .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
            .flatMap { message ->
                if (message.viewedBy.contains(memberId)) {
                    Mono.empty()
                } else {
                    val update = Update()
                        .addToSet("viewedBy", memberId)
                        .set("status", MessageStatus.VIEWED)

                    mongoTemplate.updateFirst(
                        Query(Criteria.where("_id").`is`(messageId)),
                        update,
                        Message::class.java
                    ).flatMap { result ->
                        if (result.modifiedCount > 0) {
                            messageNotificationService.notifyChatMembersAboutMessageAction(memberId, chatId, messageId, MessageAction.VIEWED)
                        } else {
                            Mono.empty()
                        }
                    }
                }
            }
    }

    private fun checkIfUserMember(userId: UUID, chatId: UUID): Mono<UUID> {
        return chatService.getChatMemberInfo(chatId, userId)
    }

    private fun checkIfMemberSender(memberId: UUID, messageId: UUID): Mono<Void> {
        return messageRepository.findById(messageId)
            .flatMap { message ->
                if (message.memberId == memberId) {
                    Mono.empty()
                } else {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_SENDER))
                }
            }
    }
}


/*
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
*/

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