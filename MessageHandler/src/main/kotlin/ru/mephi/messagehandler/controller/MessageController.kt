package ru.mephi.messagehandler.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.MessageAction
import ru.mephi.messagehandler.models.dto.rest.request.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.rest.request.MessageSearchDTO
import ru.mephi.messagehandler.models.dto.rest.request.MessageUpdateDTO
import ru.mephi.messagehandler.models.dto.rest.response.RequestResult
import ru.mephi.messagehandler.models.dto.rest.response.UnreadChanges
import ru.mephi.messagehandler.database.entity.Message
import ru.mephi.messagehandler.models.service.MessageReadReceiptService
import ru.mephi.messagehandler.models.service.MessageService
import java.util.*

@RestController
class MessageController(
    private val messageService: MessageService,
    private val messageReadReceiptService: MessageReadReceiptService
) {
    @GetMapping("/chats/{chatId}/messages/{startMessageId}")
    fun getMessagesBefore(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable startMessageId: UUID
    ): Flux<Message> {
        return messageService.getMessagesBefore(userId, chatId, startMessageId)
    }

    @PatchMapping("/chats/{chatId}/messages/{messageId}")
    fun updateMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID,
        @RequestBody updatedMessage: MessageUpdateDTO
    ): Mono<RequestResult> {
        return messageService.updateMessage(userId, chatId, messageId, updatedMessage)
    }

    @DeleteMapping("/chats/{chatId}/messages/{messageId}")
    fun deleteMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID
    ): Mono<RequestResult> {
        return messageService.deleteMessage(userId, chatId, messageId)
    }

    @PostMapping("/chats/{chatId}/messages")
    fun createMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody newMessage: MessageCreateDTO
    ): Mono<RequestResult> {
        return messageService.createMessage(userId, chatId, newMessage)
    }

    @PostMapping("/messages")
    fun searchMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @RequestBody messageSearchDTO: MessageSearchDTO
    ): Flux<Message> {
        return messageService.searchMessages(userId, messageSearchDTO)
    }

    @PostMapping("/chats/{chatId}/users")
    fun createMessageReadReceipt(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<Void> {
        return messageReadReceiptService.create(userId, chatId)
    }

    @DeleteMapping("/chats/{chatId}/users")
    fun deleteMessageReadReceipt(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<Void> {
        return messageReadReceiptService.delete(userId, chatId)
    }

    @DeleteMapping("/chats/{chatId}")
    fun deleteChat(
        @PathVariable chatId: UUID
    ): Mono<RequestResult> {
        return messageService.deleteChat(chatId)
    }

    @GetMapping("/chats/{chatId}/messages")
    fun getUnreadChanges(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<UnreadChanges> {
        return messageService.getUnreadChanges(userId, chatId)
    }

    @PostMapping("/chats/{chatId}/messages/{messageId}")
    fun markAsHandled(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID,
        @RequestParam action: MessageAction
    ): Mono<RequestResult> {
        return messageService.markAsHandledMessage(userId, chatId, messageId, action)
    }
}


/*

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
*/
