package ru.mephi.websocket.model.mapper

import org.springframework.stereotype.Component
import ru.mephi.websocket.kafka.dto.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.kafka.dto.send.ChatActivityChangeOutgoingMessage
import ru.mephi.websocket.model.dto.websocket.receive.ChatActivityChangeIngoingNotification
import ru.mephi.websocket.model.dto.websocket.send.ChatActivityChangeOutgoingNotification
import java.util.*

@Component
class ActivityStatusMapper {
    fun messageAsNotification(
        message: ChatActivityChangeIngoingMessage
    ): ChatActivityChangeOutgoingNotification {
        val notification = ChatActivityChangeOutgoingNotification(
            chatID = message.chatId,
            memberId = message.memberId,
            status = message.status,
        )
        return notification
    }

    fun notificationAsMessage(
        notification: ChatActivityChangeIngoingNotification,
        userWhoChangedStatus: UUID
    ): ChatActivityChangeOutgoingMessage {
        val message = ChatActivityChangeOutgoingMessage(
            chatId = notification.chatId,
            userId = userWhoChangedStatus,
            status = notification.status
        )
        return message
    }
}