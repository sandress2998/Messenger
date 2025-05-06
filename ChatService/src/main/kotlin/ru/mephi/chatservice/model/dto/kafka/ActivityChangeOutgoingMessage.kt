package ru.mephi.chatservice.model.dto.kafka

import ru.mephi.chatservice.model.ActivityStatus
import java.util.*

data class ActivityChangeOutgoingMessage (
    val userId: UUID, // кому нужно в итоге отправить уведомление об изменении статуса
    val chatId: UUID,
    val memberId: UUID, // тот, у которого поменялся статус
    val status: ActivityStatus
)