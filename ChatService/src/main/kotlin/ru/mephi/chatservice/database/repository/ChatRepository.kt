package ru.mephi.chatservice.database.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.chatservice.database.entity.Chat
import java.util.UUID

@Repository
interface ChatRepository : ReactiveCrudRepository<Chat, UUID> {
    fun save(chat: Chat): Mono<Chat>

    @Query("""
        UPDATE chats
        SET name = :name
        WHERE id = :chatId
        RETURNING id, name;
    """)
    fun update(chatId: UUID, name: String): Mono<Chat>
}