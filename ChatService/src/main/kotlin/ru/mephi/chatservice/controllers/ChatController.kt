package ru.mephi.chatservice.controllers

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.entity.ChatMember
import ru.mephi.chatservice.models.entity.Chat
import ru.mephi.chatservice.models.dto.MemberInfoDTO
import java.util.*


@RestController
class ChatController(
    private val chatService: ru.mephi.chatservice.service.ChatService
) {
    @GetMapping("/chats/{chatId}")
    fun getChat(@PathVariable("chatId") chatId: UUID): Mono<Chat> {
        return chatService.getChatByChatId(chatId)
    }

    // Возможно, нужно изменить URL для удобности работы с gateway
    // Нужно добавить пагинацию
    @GetMapping("/users/{userId}/chats")
    fun getChatsForUser(@PathVariable("userId") userId: UUID): Flux<Chat> {
        return chatService.getChatsByMemberId(userId)
    }

    // Нужно добавить пагинацию
    @GetMapping("/chats/{chatId}/members")
    fun getMembersForChat(@PathVariable("chatId") chatId: UUID): Flux<ChatMember> {
        return chatService.getChatMembersByChatId(chatId)
    }

    @PostMapping("/chats")
    fun createChat(@RequestBody chat: Chat): Mono<Chat> {
        return chatService.createChat(chat)
    }

    @PatchMapping("/chats/{chatId}")
    fun updateChat(@PathVariable("chatId") chatId: UUID, @RequestBody chat: Chat): Mono<ru.mephi.chatservice.models.entity.Chat> {
        return chatService.updateChat(chat.copy(id = chatId))
    }

    @DeleteMapping("/chats/{chatId}")
    fun deleteChat(@PathVariable("chatId") chatId: UUID): Mono<Void> {
        return chatService.deleteChat(chatId)
    }

    @PostMapping("/chats/{chatId}/members")
    fun addMemberToChat(
        @PathVariable("chatId") chatId: UUID,
        @RequestBody memberInfoDTO: MemberInfoDTO
    ): Mono<ChatMember> {
        return chatService.addChatMemberToChat(
            ChatMember(
                chatId = chatId,
                userId = memberInfoDTO.userId,
                role = memberInfoDTO.role
            ),
        )
    }

    @PatchMapping("/chats/{chatId}/members/{userId}")
    fun updateMemberToChat(
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("userId") userId: UUID,
        @RequestBody memberInfoDTO: MemberInfoDTO
    ): Mono<ChatMember> {
        return chatService.updateChatMemberToChat(
            ChatMember(chatId = chatId, userId = userId, role = memberInfoDTO.role),
        )
    }

    @DeleteMapping("/chats/{chatId}/members/{userId}")
    fun removeMemberFromChat(
        @PathVariable("chatId") chatId: UUID,
        @PathVariable("userId") userId: UUID
    ): Mono<Void> {
        return chatService.deleteChatMemberFromChat(chatId, userId)
    }
}