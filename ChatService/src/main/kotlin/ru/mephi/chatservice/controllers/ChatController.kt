package ru.mephi.chatservice.controllers

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.dto.MemberCreationRequest
import ru.mephi.chatservice.models.dto.MemberFromUserCreationRequest
import ru.mephi.chatservice.models.dto.RequestResult
import ru.mephi.chatservice.models.entity.Chat
import ru.mephi.chatservice.models.entity.ChatMember
import ru.mephi.chatservice.service.ChatService
import java.util.*


@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatService: ChatService
) {
    // Возможно, нужно изменить URL для удобности работы с gateway
    // Нужно добавить пагинацию
    @GetMapping
    fun getChatsForUser(@RequestHeader("X-UserId") userId: UUID): Flux<RequestResult> {
        return chatService.getChatsInfoByUserId(userId)
    }

    // Добавить механизм присвоения тому, кто создал чат, роли админа (создателя)
    @PostMapping
    fun createChat(
        @RequestHeader("X-UserId") userId: UUID,
        @RequestBody chat: Chat
    ): Mono<RequestResult> {
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
    ): Flux<RequestResult> {
        return chatService.getChatMembersByChatId(chatId, userId)
    }

    @PostMapping("/{chatId}/members")
    fun addUserToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID,
        @RequestBody memberCreationRequest: MemberCreationRequest
    ): Mono<RequestResult> {
        return chatService.addMemberToChat(
            memberCreationRequest, chatId, userInitiatorId
        )
    }

    @PutMapping("/{chatId}/members/{memberId}")
    fun updateMemberToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет изменить информацию о человеке в чате
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberId: UUID,
        @RequestBody chatMember: ChatMember
    ): Mono<RequestResult> {
        return chatService.updateChatMemberToChat(
            chatMember.copy(id = memberId, chatId = chatId), userInitiatorId
        )
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
    ): Mono<RequestResult> {
        return chatService.addUserToChat(
            creationRequest, chatId, userInitiatorId
        )
    }

    // здесь кончаются запросы, которые протестированы
}