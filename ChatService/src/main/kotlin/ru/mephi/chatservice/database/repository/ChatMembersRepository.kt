package ru.mephi.chatservice.database.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.chatservice.database.entity.ChatMember
import ru.mephi.chatservice.model.ChatRole
import java.util.*


@Repository
interface ChatMembersRepository: ReactiveCrudRepository<ChatMember, UUID> {
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getById(id: UUID): Mono<ChatMember>

    @Modifying
    @Query("UPDATE chats_members SET excluded = true WHERE id = :id AND NOT excluded;")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun deleteChatMemberById(id: UUID): Mono<Void>

    @Query("SELECT * FROM chats_members WHERE user_id = :userId AND NOT excluded;")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getChatMembersByUserId(userId: UUID): Flux<ChatMember>

    @Query("SELECT * FROM chats_members WHERE chat_id = :chatId AND NOT excluded;")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getChatMembersByChatId(chatId: UUID): Flux<ChatMember>

    // функция реально удаляет из базы данных всех участников, а не просто присваивает excluded = true
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun deleteChatMembersByChatId(chatId: UUID): Mono<Void>

    @Query("SELECT * FROM chats_members WHERE chat_id=:chatId AND user_id = :userId AND NOT excluded;")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getChatMemberByChatIdAndUserId(chatId: UUID, userId: UUID): Mono<ChatMember>

    @Query("SELECT count(*) FROM chats_members WHERE chat_id = :chatId AND NOT excluded;")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun countByChatId(chatId: UUID): Mono<Long>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun save(chatMember: ChatMember): Mono<ChatMember>

    @Query("SELECT COUNT(*) FROM chats_members WHERE chat_id = :chatId AND role = 'ADMIN' AND NOT excluded")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getAdminCount(chatId: UUID): Mono<Long>

    @Modifying
    @Query("""
        UPDATE chats_members
        SET role = :role
        WHERE id = :memberId;
    """)
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun update(memberId: UUID, role: ChatRole): Mono<Long>

    @Query("SELECT chat_id FROM chats_members WHERE user_id = :userId AND NOT excluded")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getChatsIdByUserId(userId: UUID): Flux<UUID>

    @Query("SELECT user_id FROM chats_members WHERE chat_id = :chatId AND NOT excluded")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun getUsersIdByChatId(chatId: UUID): Flux<UUID>

    @Query("SELECT chat_id FROM chats_members WHERE user_id = :userId AND NOT excluded ORDER BY RANDOM() LIMIT 2")
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun findRandomChatIdsByUserId(userId: UUID): Flux<UUID>

    @Query(
        """
    WITH candidate AS (
        SELECT id, user_id 
        FROM chats_members 
        WHERE chat_id = :chatId 
        AND NOT excluded
        ORDER BY random() 
        LIMIT 1
    )
    UPDATE chats_members 
    SET role = 'ADMIN' 
    WHERE id IN (SELECT id FROM candidate)
    RETURNING *;
    """
    )
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun promoteRandomMemberToAdmin(chatId: UUID): Mono<ChatMember>

    @Query("UPDATE chats_members SET excluded = false, role = 'MEMBER' WHERE chat_id = :chatId AND user_id = :userId AND excluded RETURNING *;")
    fun returnUserToChat(chatId: UUID, userId: UUID): Mono<ChatMember>
}