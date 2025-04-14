package ru.mephi.chatservice.controllers

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.ChatRole
import ru.mephi.chatservice.models.dto.*
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
    fun getChatsForUser(@RequestHeader("X-UserId") userId: UUID): Flux<ChatInfoResponse> {
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
    ): Flux<MemberInfoResponse> {
        return chatService.getChatMembersByChatId(chatId, userId)
    }

    @PostMapping("/{chatId}/members")
    fun addUserToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID,
        @RequestBody memberCreationRequest: MemberCreationRequest
    ): Mono<MemberInfoResponse> {
        return chatService.addMemberToChat(
            memberCreationRequest, chatId, userInitiatorId
        )
    }

    // по-хорошему здесь нужен patch, но мне лень пересобирать изображения
    @PutMapping("/{chatId}/members/{memberId}")
    fun updateMemberToChat(
        @RequestHeader("X-UserId") userInitiatorId: UUID, // userId того, кто хочет изменить информацию о человеке в чате
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("memberId") memberId: UUID,
        @RequestBody newRole: UserRoleInChat
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
    ): Mono<MemberInfoResponse> {
        return chatService.addUserToChat (
            creationRequest, chatId, userInitiatorId
        )
    }

    // здесь кончаются запросы, которые протестированы
    @GetMapping("/{chatId}/users")
    fun getUserInChat(
        @RequestHeader("X-UserId") userId: UUID, // userId того, кто хочет добавить другого человека в чат
        @PathVariable("chatId") chatId: UUID
    ): Mono<UserRoleInChat> {
        return chatService.getUserRoleInChat(chatId, userId)
    }

    // функция api для другого микросервиса
    @GetMapping("/id")
    fun getChatsId(@RequestHeader("X-UserId") userId: UUID): Flux<ChatId> {
        return chatService.getChatsId(userId)
    }


    /*
    // функция api для другого микросервиса
    @GetMapping("/{chatId}/users")
    fun getUsersIdInChat(
        @PathVariable("chatId") chatId: UUID
    ): Flux<UserId> {
        return chatService.getUsersIdByChatId(chatId)
    }
     */
}