package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.*
import ru.mephi.websocket.model.dto.kafka.receive.*

interface WebSocketProducerService {
    fun sendActivityStatusNotification(message: ChatActivityChangeIngoingMessage): Mono<Void>

    fun sendChatMemberActionNotification(message: ChatMemberActionIngoingMessage): Mono<Void>

    fun sendChatActionNotification(message: ChatActionIngoingMessage): Mono<Void>

    fun sendMessageActionNotification(message: MessageActionIngoingMessage): Mono<Void>

    fun sendUserActionNotification(message: UserActionIngoingMessage): Mono<Void>
}