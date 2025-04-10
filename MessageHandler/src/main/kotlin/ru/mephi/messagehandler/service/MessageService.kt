package ru.mephi.messagehandler.service

import jdk.incubator.vector.VectorOperators.Binary
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
import ru.mephi.messagehandler.models.entity.Message
import ru.mephi.messagehandler.models.entity.MessageStatus
import ru.mephi.messagehandler.repository.MessageRepository
import ru.mephi.messagehandler.webclient.ChatService
import ru.mephi.messagehandler.models.exception.AccessDeniedException
import ru.mephi.messagehandler.models.exception.FailureResult
import ru.mephi.messagehandler.models.exception.NotFoundException
import ru.mephi.messagehandler.util.UUIDUtil
import ru.mephi.messagehandler.webclient.dto.UserDataInChat
import java.time.Instant
import java.util.*


@Service
class MessageService (
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: MessageRepository,
    private val chatService: ChatService,
    private val properties: MessageProperties
) {
    fun getMessages (
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

    @Transactional
    fun updateMessage (
        userId: UUID,
        chatId: UUID,
        messageId: UUID,
        updatedMessage: MessageUpdateDTO
    ): Mono<RequestResult> {
        val binMessageId = UUIDUtil.toBinary(messageId)
        val query = Query(Criteria.where("_id").`is`(binMessageId))
        val update = Update().set("text", updatedMessage.text)

        return checkIfUserMember(userId, chatId)
            .then(mongoTemplate.findAndModify(query, update, Message::class.java))
            .switchIfEmpty(Mono.error(NotFoundException("Message not found")))
            .then(Mono.just(SuccessResult() as RequestResult))
    }

    @Transactional
    fun deleteMessage (
        userId: UUID,
        chatId: UUID,
        messageId: UUID
    ): Mono<RequestResult> {
        return checkIfUserMember(userId, chatId)
            .then(messageRepository.deleteById(messageId))
            .thenReturn(SuccessResult() as RequestResult)
    }

    @Transactional
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
            .flatMap { chatIdShell ->
                messageRepository.findByChatIdAndTextContaining(
                    chatIdShell.chatId, phraseToFind.phrase
                )
            }
    }

    @Transactional
    fun deleteAllMessages(
        chatId: UUID
    ): Mono<RequestResult> {
        return messageRepository.deleteMessageByChatId(chatId)
            .thenReturn(SuccessResult() as RequestResult)
    }
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
                ).map {
                    SuccessResult() as RequestResult
                }
            }
        }.switchIfEmpty(Mono.error(NotFoundException("Message not found")))
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
}
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