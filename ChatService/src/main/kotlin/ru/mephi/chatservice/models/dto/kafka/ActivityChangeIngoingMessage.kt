package ru.mephi.chatservice.models.dto.kafka

import ru.mephi.chatservice.models.ActivityStatus
import java.util.*

data class ActivityChangeIngoingMessage (
    val userId: UUID,
    val status: ActivityStatus
)