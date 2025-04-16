package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage

interface ChatService {
    fun sendChatActionNotification(message: ChatActionIngoingMessage): Mono<Void>
}