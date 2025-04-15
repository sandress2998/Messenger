package ru.mephi.presence.kafka.dto

import ru.mephi.presence.model.ActivityStatus
import java.util.*

data class ChatActivityChangeBroadcast (
    val chatId: UUID,
    val memberId: UUID,
    val status: ActivityStatus,
    val receiver: UUID
)