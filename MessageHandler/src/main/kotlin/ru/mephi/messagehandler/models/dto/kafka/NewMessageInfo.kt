package ru.mephi.messagehandler.models.dto.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import ru.mephi.messagehandler.database.entity.Message
import ru.mephi.messagehandler.database.entity.MessageStatus
import java.time.Instant
import java.util.*

data class NewMessageInfo (
    val text: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val timestamp: Instant,
    val status: MessageStatus = MessageStatus.NOT_VIEWED,
    val viewedBy: MutableList<UUID> = mutableListOf()
): MessageInfo() {
    companion object {
        fun messageAsNotification(message: Message): NewMessageInfo {
            return NewMessageInfo (
                text = message.text,
                timestamp = message.timestamp,
                status = message.status,
                viewedBy = message.viewedBy
            )
        }
    }
}