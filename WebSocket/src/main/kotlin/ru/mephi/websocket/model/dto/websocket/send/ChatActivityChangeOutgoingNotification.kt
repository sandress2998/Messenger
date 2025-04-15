package ru.mephi.websocket.model.dto.websocket.send

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.UUID


// ПЕРЕДЕЛАТЬ: ВМЕСТ EMAIL ПОЛЬЗОВАТЕЛЯ НУЖНО ИСПОЛЬЗОВАТЬ НОМЕР (ID) ПОЛЬЗОВАТЕЛЯ
data class ChatActivityChangeOutgoingNotification (
    val chatId: UUID,
    val memberId: UUID, // пользователь, который поменял статус
    val status: ActivityStatus
): BaseSendNotification() {
    override val category = "activity_status"
}