package ru.mephi.chatservice.model.dto.kafka

import ru.mephi.chatservice.model.ActivityStatus
import java.util.*

data class ActivityChangeIngoingMessage (
    val userId: UUID,
    val status: ActivityStatus
)