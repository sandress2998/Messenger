package ru.mephi.chatservice.controllers

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*


@RestController
class ChatController(
    private val chatService: ru.mephi.chatservice.service.ChatService
) {
    @GetMapping("/chats/{chatId}")
    fun getChat(@PathVariable("chatId") chatId: UUID): Mono<ru.mephi.chatservice.models.entity.Chat> {
        return chatService.getChatByChatId(chatId)
    }

    @GetMapping("/users/{userId}/chats")
    fun getChatsForUser(@PathVariable("userId") userId: UUID): Flux<ru.mephi.chatservice.models.entity.Chat> {
        return chatService.getChatsByMemberId(userId)
    }

    @GetMapping("/chats/{chatId}/members")
    fun getMembersForChat(@PathVariable("chatId") chatId: UUID): Flux<ru.mephi.chatservice.models.entity.ChatMember> {
        return chatService.getChatMembersByChatId(chatId)
    }

    @PostMapping("/chats")
    fun createChat(@RequestBody chat: ru.mephi.chatservice.models.entity.Chat): Mono<ru.mephi.chatservice.models.entity.Chat> {
        return chatService.createChat(chat)
    }
    @PatchMapping("/chats/{chatId}")
    fun updateChat(@PathVariable("chatId") chatId: UUID, @RequestBody chat: ru.mephi.chatservice.models.entity.Chat): Mono<ru.mephi.chatservice.models.entity.Chat> {
        return chatService.updateChat(chat.copy(id = chatId))
    }
    @DeleteMapping("/chats/{chatId}")
    fun deleteChat(@PathVariable("chatId") chatId: UUID): Mono<Void> {
        return chatService.deleteChat(chatId)
    }

    @PostMapping("/chats/{chatId}/members")
    fun addMemberToChat(
        @PathVariable("chatId") chatId: UUID,
        @RequestBody memberInfoDTO: ru.mephi.chatservice.models.dto.MemberInfoDTO
    ): Mono<ru.mephi.chatservice.models.entity.ChatMember> {
        return chatService.addChatMemberToChat(
            ru.mephi.chatservice.models.entity.ChatMember(
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
        @RequestBody memberInfoDTO: ru.mephi.chatservice.models.dto.MemberInfoDTO
    ): Mono<ru.mephi.chatservice.models.entity.ChatMember> {
        return chatService.updateChatMemberToChat(
            ru.mephi.chatservice.models.entity.ChatMember(chatId = chatId, userId = userId, role = memberInfoDTO.role),
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