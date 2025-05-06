package ru.mephi.websocket.model.dto.websocket.send

import ru.mephi.websocket.shared.data.ChatInfo
import ru.mephi.websocket.shared.enums.ChatAction
import java.util.*

class ChatActionNotification (
    val action: ChatAction,
    val chatId: UUID,
    val updatedChat: ChatInfo?
): BaseSendNotification() {
    override val category = NotificationSendCategory.CHAT_ACTION
}