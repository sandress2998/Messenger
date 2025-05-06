package ru.mephi.websocket.model.dto.websocket.send

import ru.mephi.websocket.shared.data.MessageInfo
import ru.mephi.websocket.shared.enums.MessageAction
import java.util.*

class MessageActionNotification(
    val action: MessageAction,
    val messageId: UUID,
    val chatId: UUID,
    val memberId: UUID,
    val messageInfo: MessageInfo?
): BaseSendNotification() {
    override val category = NotificationSendCategory.MESSAGE_ACTION
}