package ru.mephi.websocket.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.websocket.receive.ChatActivityChangeIngoingNotification
import ru.mephi.websocket.model.mapper.ActivityStatusMapper
import ru.mephi.websocket.model.service.ActivityStatusService
import ru.mephi.websocket.model.service.WebSocketNotificationProcessor

@Service
class WebSocketNotificationProcessorImpl(
    private val activityStatusService: ActivityStatusService,
    private val activityStatusMapper: ActivityStatusMapper
): WebSocketNotificationProcessor {
    override fun processActivityStatusNotification(
        notification: ChatActivityChangeIngoingNotification,
        receiver: String
    ): Mono<Void> {
        println("Десериализовано в ChatActivityChangeIngoingNotification: " +
                "chatID: ${notification.chatID}, status: ${notification.status}, receiver: $receiver")
        val outgoingMessage = activityStatusMapper.notificationAsMessage(notification, receiver)
        return activityStatusService.sendStatusUpdateMessage(outgoingMessage)
    }
}