package ru.mephi.messagehandler.model.dto.kafka

import ru.mephi.messagehandler.model.MessageAction
import java.util.*

data class MessageActionOutgoingMessage (
    val userId: UUID,
    val action: MessageAction,
    val messageId: UUID,
    val chatId: UUID,
    val memberId: UUID,
    val messageInfo: MessageInfo? = null
)

