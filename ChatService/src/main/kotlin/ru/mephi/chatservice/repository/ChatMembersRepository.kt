package ru.mephi.chatservice.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.ChatRole
import ru.mephi.chatservice.models.entity.ChatMember
import java.util.*


@Repository
interface ChatMembersRepository: ReactiveCrudRepository<ChatMember, UUID> {
    fun getById(id: UUID): Mono<ChatMember>

    fun deleteChatMemberById(id: UUID): Mono<Void>

    fun getChatMembersByUserId(userId: UUID): Flux<ChatMember>

    fun getChatMembersByChatId(chatId: UUID): Flux<ChatMember>

    fun deleteChatMembersByChatId(chatId: UUID): Mono<Void>

    fun getChatMemberByChatIdAndUserId(chatId: UUID, userId: UUID): Mono<ChatMember>

    fun countByChatId(chatId: UUID): Mono<Long>

    fun save(chatMember: ChatMember): Mono<ChatMember>

    @Query("SELECT COUNT(*) FROM chats_members WHERE chat_id = :chatId AND  role = 'ADMIN'")
    fun getAdminCount(chatId: UUID): Mono<Long>

    @Query("""
        UPDATE chats_members
        SET role = :role
        WHERE id = :memberId;
    """)
    fun update(memberId: UUID, role: ChatRole): Mono<Long>

    @Query("SELECT chat_id FROM chats_members WHERE user_id = :userId")
    fun getChatsIdByUserId(userId: UUID): Flux<UUID>

    @Query("SELECT user_id FROM chats_members WHERE chat_id = :chatId")
    fun getUsersIdByChatId(chatId: UUID): Flux<UUID>

    @Query("SELECT chat_id FROM chats_members WHERE user_id = :userId ORDER BY RANDOM() LIMIT 2")
    fun findRandomChatIdsByUserId(userId: UUID): Flux<UUID>
}