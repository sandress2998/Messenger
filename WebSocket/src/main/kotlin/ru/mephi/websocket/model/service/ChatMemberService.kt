package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatMemberActionIngoingMessage

interface ChatMemberService {
    fun sendChatMemberActionNotification(message: ChatMemberActionIngoingMessage): Mono<Void>
}