package ru.mephi.websocket.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatMemberActionIngoingMessage
import ru.mephi.websocket.model.mapper.Mapper
import ru.mephi.websocket.model.service.ChatMemberService
import ru.mephi.websocket.model.service.SessionService

@Service
class ChatMemberServiceImpl (
    private val sessionService: SessionService,
    private val mapper: Mapper
): ChatMemberService {
    override fun sendChatMemberActionNotification(message: ChatMemberActionIngoingMessage): Mono<Void> {
            return sessionService.sendNotification(message.userId, mapper.chatMemberActionMessageAsNotification(message))
    }
}