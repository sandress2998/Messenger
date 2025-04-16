package ru.mephi.websocket.dto.websocket.receive

import ru.mephi.websocket.dto.websocket.send.NotificationSendCategory
import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

class ChatActivityChangeIngoingNotification(
    val chatId: UUID,
    val status: ActivityStatus
): BaseReceiveNotification() {
    override val category = NotificationReceiveCategory.ACTIVITY_STATUS
}