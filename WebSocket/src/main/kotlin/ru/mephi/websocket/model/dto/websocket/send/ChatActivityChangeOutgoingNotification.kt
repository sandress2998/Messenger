package ru.mephi.websocket.model.dto.websocket.send

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*


// ПЕРЕДЕЛАТЬ: ВМЕСТ EMAIL ПОЛЬЗОВАТЕЛЯ НУЖНО ИСПОЛЬЗОВАТЬ НОМЕР (ID) ПОЛЬЗОВАТЕЛЯ
class ChatActivityChangeOutgoingNotification (
    val chatId: UUID,
    val memberId: UUID, // пользователь, который поменял статус
    val status: ActivityStatus
): BaseSendNotification() {
    override val category = NotificationSendCategory.ACTIVITY_STATUS
}