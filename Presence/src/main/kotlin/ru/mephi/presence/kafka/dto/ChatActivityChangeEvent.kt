package ru.mephi.presence.kafka.dto

import ru.mephi.presence.model.ActivityStatus
import java.util.*

data class ChatActivityChangeEvent (
    val chatId: UUID,
    val userId: UUID,
    val status: ActivityStatus
)