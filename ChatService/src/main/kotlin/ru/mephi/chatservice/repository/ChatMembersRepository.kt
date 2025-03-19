package ru.mephi.chatservice.repository

import ru.mephi.chatservice.models.entity.ChatMember
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID


@Repository
interface ChatMembersRepository : ReactiveCrudRepository<ChatMember, UUID> {

    fun getChatMembersByUserId(userId: UUID): Flux<ChatMember>
    fun getChatMembersByChatId(chatId: UUID): Flux<ChatMember>
    fun deleteChatMembersByChatId(chatId: UUID): Mono<Void>
    fun getChatMemberByChatIdAndUserId(chatId: UUID, memberId: UUID): Mono<ChatMember>
    fun deleteChatMembersByChatIdAndUserId(chatId: UUID, memberId: UUID): Mono<Void>
}