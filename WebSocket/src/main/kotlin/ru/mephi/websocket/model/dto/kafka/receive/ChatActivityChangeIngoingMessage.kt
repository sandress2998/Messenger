package ru.mephi.websocket.model.dto.kafka.receive

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

data class ChatActivityChangeIngoingMessage (
    val userId: UUID, // пользователь, которого надо уведомить об изменении активности другого пользователя
    val chatId: UUID,
    val memberId: UUID, // пользователь, у которого изменился статус
    val status: ActivityStatus
)

/*
data class ChatActivityChangeIngoingMessage (
    val chatID: UUID,
    val userId: UUID, // пользователь, у которого изменился статус
    val status: ActivityStatus,
    val receiver: String // пользователь, которого надо уведомить об изменении активности другого пользователя
)
 */