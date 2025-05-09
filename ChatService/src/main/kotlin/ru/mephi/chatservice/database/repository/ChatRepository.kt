package ru.mephi.chatservice.database.repository

import io.micrometer.core.annotation.Timed
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.chatservice.database.entity.Chat
import java.util.UUID

@Repository
interface ChatRepository : ReactiveCrudRepository<Chat, UUID> {
    companion object {
        const val CLASS_NAME = "ChatRepository"
    }

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun save(chat: Chat): Mono<Chat>

    @Query("""
        UPDATE chats
        SET name = :name
        WHERE id = :chatId
        RETURNING id, name;
    """)
    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    fun update(chatId: UUID, name: String): Mono<Chat>

    @Timed(value = "db.query.time", description = "Time taken to execute database queries")
    override fun deleteById(chatId: UUID): Mono<Void>
}