package ru.mephi.websocket.kafka.dto.send

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

data class ChatActivityChangeOutgoingMessage (
    val chatId: UUID,
    val userId: UUID, // тот пользователь, у которого изменился статус
    val status: ActivityStatus
)