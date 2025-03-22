package ru.mephi.messagehandler.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.messagehandler.models.entity.Message
import ru.mephi.messagehandler.models.dto.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.MessageUpdateDTO
import ru.mephi.messagehandler.models.exception.NotFoundException
import ru.mephi.messagehandler.repository.MessageRepository
import java.util.*


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