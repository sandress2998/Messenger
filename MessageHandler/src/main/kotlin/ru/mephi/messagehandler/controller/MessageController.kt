package ru.mephi.messagehandler.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.entity.Message
import ru.mephi.messagehandler.models.dto.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.MessageUpdateDTO
import ru.mephi.messagehandler.service.MessageService
import java.util.*


@RestController
class MessageController (
    private val messageService : MessageService,
){
    @GetMapping("/chats/{chatId}/messages")
    fun getMessages(@PathVariable("chatId") chatId : UUID):  Flux<Message> {
        return messageService.getMessagesByChat(chatId)
    }

    @PatchMapping("/messages/{messageId}")
    fun updateMessage(
        @PathVariable("messageId") messageId : UUID,
        @RequestBody message: MessageUpdateDTO
    ) : Mono<Message> {
        return messageService.updateMessage(message.copy(id = messageId))
    }

    @PostMapping("/chats/{chatId}/messages")
    fun postMessage(
        @PathVariable("chatId") chatId : UUID,
        @RequestBody message: MessageCreateDTO
    ) : Mono<Message> {
        // Добавить проверку наличия таково чата
        //добаить транзакционность к отправлению сообщения в kafka
        return messageService.createMessage(message)
    }

    @DeleteMapping("/chats/{chatId}/messages")
    fun deleteMessagesInChat(@PathVariable("chatId") chatId : UUID) : Mono<Void> {
        // Добавить проверку наличия таково чата
        return messageService.deleteMessageByChatId(chatId)
    }

    @DeleteMapping("/chats/{chatId}/members/{senderId}")
    fun deleteMessagesInChatBySenderId(
        @PathVariable("chatId") chatId : UUID,
        @PathVariable("senderId") senderId : UUID
    ) : Mono<Void> {
        // Добавить проверку наличия таково чата
        return messageService.deleteMessagesByChatIdAndUserId(chatId, senderId)
    }

    @DeleteMapping("/messages/{messageId}")
    fun deleteMessage(@PathVariable("messageId") messageId : UUID) : Mono<Void> {
        return messageService.deleteMessageById(messageId)
    }
}

