package ru.mephi.websocket.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.*
import ru.mephi.websocket.model.dto.kafka.receive.*
import ru.mephi.websocket.model.mapper.Mapper
import ru.mephi.websocket.model.service.SessionService
import ru.mephi.websocket.model.service.WebSocketProducerService

@Service
class WebSocketProducerServiceImpl (
    private val sessionService: SessionService,
    private val mapper: Mapper
): WebSocketProducerService {
    override fun sendActivityStatusNotification(message: ChatActivityChangeIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.activityMessageAsNotification(message))
    }

    override fun sendChatMemberActionNotification(message: ChatMemberActionIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.chatMemberActionMessageAsNotification(message))
    }

    override fun sendChatActionNotification(message: ChatActionIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.chatActionMessageAsNotification(message))
    }

    override fun sendMessageActionNotification(message: MessageActionIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.messageActionMessageAsNotification(message))
    }

    override fun sendUserActionNotification(message: UserActionIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.userActionMessageAsNotification(message))
    }
}