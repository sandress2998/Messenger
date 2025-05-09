package ru.mephi.messagehandler.controller

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.annotation.TimeHttpRequest
import ru.mephi.messagehandler.config.TimerAspectConfig
import ru.mephi.messagehandler.database.entity.Message
import ru.mephi.messagehandler.model.MessageAction
import ru.mephi.messagehandler.model.dto.rest.request.MessageCreateDTO
import ru.mephi.messagehandler.model.dto.rest.request.MessageSearchDTO
import ru.mephi.messagehandler.model.dto.rest.request.MessageUpdateDTO
import ru.mephi.messagehandler.model.dto.rest.response.SuccessResult
import ru.mephi.messagehandler.model.dto.rest.response.UnreadChanges
import ru.mephi.messagehandler.model.service.MessageReadReceiptService
import ru.mephi.messagehandler.model.service.MessageService
import java.util.*

@RestController
class MessageController(
    private val messageService: MessageService,
    private val messageReadReceiptService: MessageReadReceiptService,
    private val timerAspectConfig: TimerAspectConfig,
    private val registry: MeterRegistry
) {
    @GetMapping("/chats/{chatId}/messages/{startMessageId}")
    @TimeHttpRequest("GET", "/chats/{chatId}/messages/{startMessageId}")
    fun getMessagesBefore(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable startMessageId: UUID
    ): Flux<Message> {
        getMessagesBeforeCounter.increment()
        return messageService.getMessagesBefore(userId, chatId, startMessageId)
    }

    @PatchMapping("/chats/{chatId}/messages/{messageId}")
    @TimeHttpRequest("PATCH", "/chats/{chatId}/messages/{messageId}")
    fun updateMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID,
        @RequestBody updatedMessage: MessageUpdateDTO
    ): Mono<SuccessResult> {
        updateMessageCounter.increment()
        return messageService.updateMessage(userId, chatId, messageId, updatedMessage)
    }

    @DeleteMapping("/chats/{chatId}/messages/{messageId}")
    @TimeHttpRequest("DELETE", "/chats/{chatId}/messages/{messageId}")
    fun deleteMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID
    ): Mono<SuccessResult> {
        deleteMessageCounter.increment()
        return messageService.deleteMessage(userId, chatId, messageId)
    }

    @PostMapping("/chats/{chatId}/messages")
    @TimeHttpRequest("POST", "/chats/{chatId}/messages")
    fun createMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody newMessage: MessageCreateDTO
    ): Mono<SuccessResult> {
        createMessageCounter.increment()
        return messageService.createMessage(userId, chatId, newMessage)
    }

    @PostMapping("/messages")
    @TimeHttpRequest("POST", "/messages")
    fun searchMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @RequestBody messageSearchDTO: MessageSearchDTO
    ): Flux<Message> {
        searchMessageCounter.increment()
        return messageService.searchMessages(userId, messageSearchDTO)
    }

    @PostMapping("/chats/{chatId}/users")
    @TimeHttpRequest("POST", "/chats/{chatId}/users")
    fun createMessageReadReceipt(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<Void> {
        createReadReceiptCounter.increment()
        return messageReadReceiptService.create(userId, chatId)
    }

    @DeleteMapping("/chats/{chatId}/users")
    @TimeHttpRequest("DELETE", "/chats/{chatId}/users")
    fun deleteMessageReadReceipt(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<Void> {
        deleteReadReceiptCounter.increment()
        return messageReadReceiptService.delete(userId, chatId)
    }

    @DeleteMapping("/chats/{chatId}")
    @TimeHttpRequest("DELETE", "/chats/{chatId}")
    fun deleteChat(
        @PathVariable chatId: UUID
    ): Mono<SuccessResult> {
        deleteChatCounter.increment()
        return messageService.deleteChat(chatId)
    }

    @GetMapping("/chats/{chatId}/messages")
    @TimeHttpRequest("GET", "/chats/{chatId}/messages")
    fun getUnreadChanges(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID
    ): Mono<UnreadChanges> {
        getUnreadChangesCounter.increment()
        return messageService.getUnreadChanges(userId, chatId)
    }

    @PostMapping("/chats/{chatId}/messages/{messageId}")
    @TimeHttpRequest("POST", "/chats/{chatId}/messages/{messageId}")
    fun markAsHandled(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID,
        @RequestParam action: MessageAction
    ): Mono<SuccessResult> {
        markAsHandledCounter.increment()
        return messageService.markAsHandledMessage(userId, chatId, messageId, action)
    }

    // Counters
    private final val getMessagesBeforeCounter: Counter
    private final val updateMessageCounter: Counter
    private final val deleteMessageCounter: Counter
    private final val createMessageCounter: Counter
    private final val searchMessageCounter: Counter
    private final val createReadReceiptCounter: Counter
    private final val deleteReadReceiptCounter: Counter
    private final val deleteChatCounter: Counter
    private final val getUnreadChangesCounter: Counter
    private final val markAsHandledCounter: Counter

    init {
        val metricName = "http.requests.total"

        getMessagesBeforeCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/messages/{startMessageId}")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/chats/{chatId}/messages/{startMessageId}")
            }

        updateMessageCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/messages/{messageId}")
            .tag("method", "PATCH")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("PATCH", "/chats/{chatId}/messages/{messageId}")
            }

        deleteMessageCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/messages/{messageId}")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE", "/chats/{chatId}/messages/{messageId}")
            }

        createMessageCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/messages")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST", "/chats/{chatId}/messages")
            }

        searchMessageCounter = Counter.builder(metricName)
            .tag("endpoint", "/messages")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST", "/messages")
            }

        createReadReceiptCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/users")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST", "/chats/{chatId}/users")
            }

        deleteReadReceiptCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/users")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE", "/chats/{chatId}/users")
            }

        deleteChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE", "/chats/{chatId}")
            }

        getUnreadChangesCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/messages")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/chats/{chatId}/messages")
            }

        markAsHandledCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/messages/{messageId}")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST", "/chats/{chatId}/messages/{messageId}")
            }
    }
}

/* оставляю на всякий случай
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
    ): Mono<SuccessResult> {
        return messageService.updateMessage(userId, chatId, messageId, updatedMessage)
    }

    @DeleteMapping("/chats/{chatId}/messages/{messageId}")
    fun deleteMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @PathVariable messageId: UUID
    ): Mono<SuccessResult> {
        return messageService.deleteMessage(userId, chatId, messageId)
    }

    @PostMapping("/chats/{chatId}/messages")
    fun createMessage(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody newMessage: MessageCreateDTO
    ): Mono<SuccessResult> {
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
    ): Mono<SuccessResult> {
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
    ): Mono<SuccessResult> {
        return messageService.markAsHandledMessage(userId, chatId, messageId, action)
    }
}
*/


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
