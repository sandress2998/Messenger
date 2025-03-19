package ru.mephi.chatservice.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.mephi.chatservice.models.entity.Chat
import java.util.UUID

@Repository
interface ChatRepository : ReactiveCrudRepository<Chat, UUID> {
    fun getChatById(id: UUID): Mono<Chat>
    fun deleteChatById(id: UUID): Mono<Void>
}