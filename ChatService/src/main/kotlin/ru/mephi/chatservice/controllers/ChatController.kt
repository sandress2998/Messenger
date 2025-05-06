package ru.mephi.chatservice.controllers

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.model.dto.rest.*
import ru.mephi.chatservice.database.entity.Chat
import ru.mephi.chatservice.model.service.ActivityService
import ru.mephi.chatservice.model.service.ChatService
import java.util.*


@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatService: ChatService,
    private val activityService: ActivityService
) {
    // Возможно, нужно изменить URL для удобности работы с gateway
    // Нужно добавить пагинацию
    @GetMapping
    fun getChatsForUser(@RequestHeader("X-UserId") userId: UUID): Flux<ChatInfo> {
        return chatService.getChatsInfoByUserId(userId)
    }

    // Добавить механизм присвоения тому, кто создал чат, роли админа (создателя)
    @PostMapping
    fun createChat(
        @RequestHeader("X-UserId") userId: UUID,
        @RequestBody chat: Chat
    ): Mono<ChatCreationResponse> {
        return chatService.createChat(chat, userId)
    }

    @PutMapping("/{chatId}")
    fun updateChat(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody chat: Chat
    ): Mono<RequestResult> {
        return chatService.updateChat(chat.copy(id = chatId), chatId, userId)
    }

    @DeleteMapping("/{chatId}")
    fun deleteChat (
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable("chatId") chatId: UUID
    ): Mono<RequestResult> {
        return chatService.deleteChat(chatId, userId)
    }

    // Нужно добавить пагинацию
    @GetMapping("/{chatId}/members")
    fun getMembersForChat(
        @RequestHeader("X-UserId") userId: UUID,
        @PathVariable("chatId") chatId: UUID
    ): Flux<MemberInfo> {
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
    @PutMapping("/{chatId}/members/{memberId}")
    fun updateMemberToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет изменить информацию о человеке в чате
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberId: UUID,
        @RequestBody newRole: UpdateChatMemberRole
    ): Mono<RequestResult> {
        return chatService.updateChatMemberToChat(chatId, memberId, newRole.role, userInitiatorId)
    }

    @DeleteMapping("/{chatId}/members/{memberId}")
    fun removeMemberFromChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID,
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberToDeleteId: UUID
    ): Mono<RequestResult> {
        return chatService.deleteMemberFromChat(chatId, memberToDeleteId, userInitiatorId)
    }

    // изменить на добавление по какому-то тэгу (или лейблу, как это называется...)
    @PostMapping("/{chatId}/users")
    fun addUserByEmail(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID,
        @RequestBody creationRequest: MemberFromUserCreationRequest
    ): Mono<MemberInfo> {
        return chatService.addUserToChat (
            creationRequest, chatId, userInitiatorId
        )
    }

    // здесь кончаются запросы, которые протестированы
    @GetMapping("/{chatId}/member")
    fun getUserInChat(
        @RequestHeader("X-UserId") userId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID
    ): Mono<ChatMemberInfo> {
        return chatService.getChatMemberId(chatId, userId)
    }

    // функция api для другого микросервиса
    @GetMapping("/id")
    fun getChatsId(@RequestHeader("X-UserId") userId: UUID): Flux<ChatId> {
        return chatService.getChatsId(userId)
    }

    @GetMapping("/{chatId}/members/active")
    fun getActiveUsersInChat(@PathVariable("chatId") chatId: UUID): Flux<UserId> {
        return activityService.getActiveUsersInChat(chatId)
    }

    @GetMapping("/{chatId}/members/{memberId}")
    fun getUserInfoByMemberId(
        @RequestHeader("X-UserId") userInitiatorId: UUID,
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberId: UUID
    ): Mono<UserInfo> {
        return chatService.getUserInfoByMemberId(chatId, memberId, userInitiatorId)
    }
}