package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatMemberActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.MessageActionIngoingMessage

interface WebSocketProducerService {
    fun sendActivityStatusNotification(message: ChatActivityChangeIngoingMessage): Mono<Void>

    fun sendChatMemberActionNotification(message: ChatMemberActionIngoingMessage): Mono<Void>

    fun sendChatActionNotification(message: ChatActionIngoingMessage): Mono<Void>

    fun sendMessageActionNotification(message: MessageActionIngoingMessage): Mono<Void>
}