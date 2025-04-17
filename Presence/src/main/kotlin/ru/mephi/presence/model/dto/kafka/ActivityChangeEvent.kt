package ru.mephi.presence.model.dto.kafka

import ru.mephi.presence.model.ActivityStatus
import java.util.*

data class ActivityChangeEvent (
    val userId: UUID,
    val status: ActivityStatus
)