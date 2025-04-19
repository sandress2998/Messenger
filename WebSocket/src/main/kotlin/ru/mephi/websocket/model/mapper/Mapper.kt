package ru.mephi.websocket.model.mapper

import org.springframework.stereotype.Component
import ru.mephi.websocket.dto.kafka.receive.ChatActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.ChatMemberActionIngoingMessage
import ru.mephi.websocket.dto.kafka.receive.MessageActionIngoingMessage
import ru.mephi.websocket.dto.websocket.send.ChatActionNotification
import ru.mephi.websocket.dto.websocket.send.ChatActivityChangeOutgoingNotification
import ru.mephi.websocket.dto.websocket.send.ChatMemberActionNotification
import ru.mephi.websocket.dto.websocket.send.MessageActionNotification

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
}