package ru.mephi.websocket.model.mapper

import org.springframework.stereotype.Component
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatMemberActionIngoingMessage
import ru.mephi.websocket.dto.kafka.send.ActivityChangeOutgoingMessage
import ru.mephi.websocket.dto.websocket.receive.ChatActivityChangeIngoingNotification
import ru.mephi.websocket.dto.websocket.send.ChatActionNotification
import ru.mephi.websocket.dto.websocket.send.ChatActivityChangeOutgoingNotification
import ru.mephi.websocket.dto.websocket.send.ChatMemberActionNotification
import ru.mephi.websocket.shared.data.ChatInfo
import java.util.*

@Component
class Mapper {
    fun activityMessageAsNotification(
        message: ChatActivityChangeIngoingMessage
    ): ChatActivityChangeOutgoingNotification {
        val notification = ChatActivityChangeOutgoingNotification(
            chatId = message.chatId,
            memberId = message.memberId,
            status = message.status,
        )
        return notification
    }

    fun activityNotificationAsMessage(
        notification: ChatActivityChangeIngoingNotification,
        userWhoChangedStatus: UUID
    ): ActivityChangeOutgoingMessage {
        val message = ActivityChangeOutgoingMessage(
            userId = userWhoChangedStatus,
            status = notification.status
        )
        return message
    }

    fun chatActionMessageAsNotification(message: ChatActionIngoingMessage): ChatActionNotification {
        val notification = ChatActionNotification(
            chatId = message.chatId,
            action = message.action,
            updatedChat = message.updatedChat
        )
        return notification
    }

    fun chatMemberActionMessageAsNotification(message: ChatMemberActionIngoingMessage): ChatMemberActionNotification {
        val notification = ChatMemberActionNotification(
            action = message.action,
            chatId = message.chatId,
            memberId = message.memberId,
            member = message.member
        )
        return notification
    }
}