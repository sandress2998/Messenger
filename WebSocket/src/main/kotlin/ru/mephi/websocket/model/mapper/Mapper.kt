package ru.mephi.websocket.model.mapper

import org.springframework.stereotype.Component
import ru.mephi.websocket.dto.kafka.receive.*
import ru.mephi.websocket.dto.websocket.send.*
import ru.mephi.websocket.model.dto.kafka.receive.*
import ru.mephi.websocket.model.dto.websocket.send.*

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

    fun messageActionMessageAsNotification(message: MessageActionIngoingMessage): MessageActionNotification {
        val notification = MessageActionNotification (
            action = message.action,
            messageId = message.messageId,
            chatId = message.chatId,
            memberId = message.memberId,
            messageInfo = message.messageInfo
        )
        return notification
    }

    fun userActionMessageAsNotification(message: UserActionIngoingMessage): UserActionOutgoingNotification {
        val notification = UserActionOutgoingNotification (
            action = message.action,
            userInfo = message.userInfo
        )

        return notification
    }
}