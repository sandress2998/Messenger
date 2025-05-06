package ru.mephi.websocket.model.dto.kafka.send

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

data class ActivityChangeOutgoingMessage (
    val userId: UUID, // тот пользователь, у которого изменился статус
    val status: ActivityStatus
)