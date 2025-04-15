package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.websocket.receive.ChatActivityChangeIngoingNotification
import java.util.*

interface WebSocketNotificationProcessor {
    fun processActivityStatusNotification(
        notification: ChatActivityChangeIngoingNotification,
        receiver: UUID
    ): Mono<Void>
}