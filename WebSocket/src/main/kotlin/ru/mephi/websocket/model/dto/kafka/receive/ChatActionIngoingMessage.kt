package ru.mephi.websocket.model.dto.kafka.receive

import ru.mephi.websocket.shared.data.ChatInfo
import ru.mephi.websocket.shared.enums.ChatAction
import java.util.*

data class ChatActionIngoingMessage (
    val action: ChatAction,
    val userId: UUID,
    val chatId: UUID,
    val updatedChat: ChatInfo?
)