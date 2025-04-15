package ru.mephi.websocket.kafka.dto.receive

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

data class ChatActivityChangeIngoingMessage (
    val chatId: UUID,
    val memberId: UUID, // пользователь, у которого изменился статус
    val status: ActivityStatus,
    val receiver: UUID // пользователь, которого надо уведомить об изменении активности другого пользователя
)

/*
data class ChatActivityChangeIngoingMessage (
    val chatID: UUID,
    val userId: UUID, // пользователь, у которого изменился статус
    val status: ActivityStatus,
    val receiver: String // пользователь, которого надо уведомить об изменении активности другого пользователя
)
 */