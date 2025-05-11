package ru.mephi.chatservice.controllers

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.annotation.TimeHttpRequest
import ru.mephi.chatservice.config.TimerAspectConfig
import ru.mephi.chatservice.database.entity.Chat
import ru.mephi.chatservice.model.dto.rest.*
import ru.mephi.chatservice.model.service.ActivityService
import ru.mephi.chatservice.model.service.ChatService
import java.util.*


@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatService: ChatService,
    private val activityService: ActivityService,
    private val timerAspectConfig: TimerAspectConfig,
    private val registry: MeterRegistry
) {
    // Нужно добавить пагинацию
    @GetMapping
    @TimeHttpRequest("GET","/chats")
    fun getChatsForUser(@RequestHeader("X-UserId") userId: UUID): Flux<ChatInfo> {
        getChatsForUserCounter.increment()
        return chatService.getChatsInfoByUserId(userId)
    }

    @PostMapping
    @TimeHttpRequest("POST", "/chats")
    fun createChat(
        @RequestHeader("X-UserId") userId: UUID,
        @RequestBody chat: Chat
    ): Mono<ChatCreationResponse> {
        createChatCounter.increment()
        return chatService.createChat(chat, userId)
    }

    @PutMapping("/{chatId}")
    @TimeHttpRequest("PUT", "/chats/{chatId}")
    fun updateChat(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody chat: Chat
    ): Mono<RequestResult> {
        updateChatCounter.increment()
        return chatService.updateChat(chat.copy(id = chatId), chatId, userId)
    }

    @DeleteMapping("/{chatId}")
    @TimeHttpRequest("DELETE", "/chats/{chatId}")
    fun deleteChat (
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable("chatId") chatId: UUID
    ): Mono<Void> {
        deleteChatCounter.increment()
        return chatService.deleteChat(chatId, userId)
    }

    // Нужно добавить пагинацию
    @GetMapping("/{chatId}/members")
    @TimeHttpRequest("GET", "/chats/{chatId}/members")
    fun getMembersForChat(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable("chatId") chatId: UUID
    ): Flux<MemberInfo> {
        getMembersForChatCounter.increment()
        return chatService.getChatMembersByChatId(chatId, userId)
    }

    /*
    @PostMapping("/{chatId}/members")
    fun addUserToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID,
        @RequestBody memberCreationRequest: MemberCreationRequest
    ): Mono<MemberInfo> {
        return chatService.addMemberToChat(
            memberCreationRequest, chatId, userInitiatorId
        )
    }
     */

    // по-хорошему здесь нужен patch, но мне лень пересобирать изображения
    @PatchMapping("/{chatId}/members/{memberId}")
    @TimeHttpRequest("PATCH", "/chats/{chatId}/members/{memberId}")
    fun updateMemberToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет изменить информацию о человеке в чате
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberId: UUID,
        @RequestBody newRole: UpdateChatMemberRole
    ): Mono<RequestResult> {
        updateMemberToChatCounter.increment()
        return chatService.updateChatMemberToChat(chatId, memberId, newRole.role, userInitiatorId)
    }

    @DeleteMapping("/{chatId}/members/{memberId}")
    @TimeHttpRequest("DELETE", "/chats/{chatId}/members/{memberId}")
    fun removeMemberFromChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID,
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberToDeleteId: UUID
    ): Mono<Void> {
        removeMemberFromChatCounter.increment()
        return chatService.deleteMemberFromChat(chatId, memberToDeleteId, userInitiatorId)
    }

    // изменить на добавление по какому-то тэгу (или лейблу, как это называется...)
    @PostMapping("/{chatId}/users")
    @TimeHttpRequest("POST", "/chats/{chatId}/users")
    fun addUserByEmail(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID,
        @RequestBody creationRequest: MemberFromUserCreationRequest
    ): Mono<MemberInfo> {
        addUserByEmailCounter.increment()
        return chatService.addUserToChat (
            creationRequest, chatId, userInitiatorId
        )
    }

    @GetMapping("/{chatId}/member")
    @TimeHttpRequest("GET", "/chats/{chatId}/member")
    fun getUserInChat(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable("chatId") chatId: UUID
    ): Mono<ChatMemberInfo> {
        getUserInChatCounter.increment()
        return chatService.getChatMemberId(chatId, userId)
    }

    // функция api для другого микросервиса
    @GetMapping("/id")
    @TimeHttpRequest("GET", "/chats/id")
    fun getChatsId(@RequestHeader("X-UserId") userId: UUID): Flux<ChatId> {
        getChatsIdCounter.increment()
        return chatService.getChatsId(userId)
    }

    @GetMapping("/{chatId}/members/active")
    @TimeHttpRequest("GET", "/chats/{chatId}/members/active")
    fun getActiveUsersInChat(@PathVariable("chatId") chatId: UUID): Flux<UserId> {
        getActiveUsersInChatCounter.increment()
        return activityService.getActiveUsersInChat(chatId)
    }

    @GetMapping("/{chatId}/members/{memberId}")
    @TimeHttpRequest("GET", "/chats/{chatId}/members/{memberId}")
    fun getUserInfoByMemberId(
        @RequestHeader("X-UserId") userInitiatorId: UUID,
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberId: UUID
    ): Mono<UserInfo> {
        getUserInfoByMemberIdCounter.increment()
        return chatService.getUserInfoByMemberId(chatId, memberId, userInitiatorId)
    }

    private final val getChatsForUserCounter: Counter
    private final val createChatCounter: Counter
    private final val updateChatCounter: Counter
    private final val deleteChatCounter: Counter
    private final val getMembersForChatCounter: Counter
    private final val updateMemberToChatCounter: Counter
    private final val removeMemberFromChatCounter: Counter
    private final val addUserByEmailCounter: Counter
    private final val getUserInChatCounter: Counter
    private final val getChatsIdCounter: Counter
    private final val getActiveUsersInChatCounter: Counter
    private final val getUserInfoByMemberIdCounter: Counter

    init {
        val metricName = "requests.total"

        getChatsForUserCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET","/chats")
            }

        createChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST","/chats" )
            }

        updateChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}")
            .tag("method", "PUT")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("PUT", "/chats/{chatId}")
            }

        deleteChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE","/chats/{chatId}")
            }

        getMembersForChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/members")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/chats/{chatId}/members")
            }

        updateMemberToChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/members/{memberId}")
            .tag("method", "PUT")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("PUT","/chats/{chatId}/members/{memberId}")
            }

        removeMemberFromChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/members/{memberId}")
            .tag("method", "DELETE")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("DELETE", "/chats/{chatId}/members/{memberId}")
            }

        addUserByEmailCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/members")
            .tag("method", "POST")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("POST","/chats/{chatId}/members")
            }

        getUserInChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/member")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET","/chats/{chatId}/member")
            }

        getChatsIdCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/id")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET", "/chats/id")
            }

        getActiveUsersInChatCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/members/active")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET","/chats/{chatId}/members/active")
            }

        getUserInfoByMemberIdCounter = Counter.builder(metricName)
            .tag("endpoint", "/chats/{chatId}/members/{memberId}")
            .tag("method", "GET")
            .register(registry)
            .also {
                timerAspectConfig.setHttpRequestTimer("GET","/chats/{chatId}/members/{memberId}")
            }
    }
}