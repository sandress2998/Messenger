package ru.mephi.websocket.model.mapper

import org.springframework.stereotype.Component
import ru.mephi.websocket.model.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.model.dto.kafka.send.ChatActivityChangeOutgoingMessage
import ru.mephi.websocket.model.dto.websocket.receive.ChatActivityChangeIngoingNotification
import ru.mephi.websocket.model.dto.websocket.send.ChatActivityChangeOutgoingNotification

@Component
class ActivityStatusMapper {
    fun messageAsNotification(
        message: ChatActivityChangeIngoingMessage
    ): ChatActivityChangeOutgoingNotification {
        val notification = ChatActivityChangeOutgoingNotification(
            chatID = message.chatID,
            email = message.email,
            status = message.status,
        )
        return notification
    }

    fun notificationAsMessage(
        notification: ChatActivityChangeIngoingNotification,
        userWhoChangedStatus: String
    ): ChatActivityChangeOutgoingMessage {
        val message = ChatActivityChangeOutgoingMessage(
            chatID = notification.chatID,
            email = userWhoChangedStatus,
            status = notification.status
        )
        return message
    }
}