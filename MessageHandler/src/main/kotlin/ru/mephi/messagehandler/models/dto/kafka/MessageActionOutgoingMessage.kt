package ru.mephi.messagehandler.models.dto.kafka

import ru.mephi.messagehandler.models.MessageAction
import java.util.*

data class MessageActionOutgoingMessage (
    val userId: UUID,
    val action: MessageAction,
    val messageId: UUID,
    val chatId: UUID,
    val memberId: UUID,
    val messageInfo: MessageInfo? = null
)

