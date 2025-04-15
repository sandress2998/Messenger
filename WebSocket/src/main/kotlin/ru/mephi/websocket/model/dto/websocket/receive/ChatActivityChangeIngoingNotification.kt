package ru.mephi.websocket.model.dto.websocket.receive

import ru.mephi.websocket.shared.enums.ActivityStatus
import java.util.*

class ChatActivityChangeIngoingNotification(
    val chatId: UUID,
    val status: ActivityStatus
): BaseReceiveNotification() {
    override val category = "activity_status"
}