package ru.mephi.websocket.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage
import ru.mephi.websocket.model.mapper.Mapper
import ru.mephi.websocket.model.service.ChatService
import ru.mephi.websocket.model.service.SessionService

@Service
class ChatServiceImpl (
    private val sessionService: SessionService,
    private val mapper: Mapper
): ChatService {
    override fun sendChatActionNotification(message: ChatActionIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.chatActionMessageAsNotification(message))
    }
}