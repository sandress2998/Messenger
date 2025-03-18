package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.websocket.receive.ChatActivityChangeIngoingNotification

interface WebSocketNotificationProcessor {
    fun processActivityStatusNotification(
        notification: ChatActivityChangeIngoingNotification,
        receiver: String
    ): Mono<Void>
}