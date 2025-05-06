package ru.mephi.websocket.model.dto.websocket.send

import ru.mephi.websocket.shared.data.MemberInfo
import ru.mephi.websocket.shared.enums.ChatMemberAction
import java.util.*

class ChatMemberActionNotification (
    val action: ChatMemberAction,
    val chatId: UUID,
    val memberId: UUID,
    val member: MemberInfo?
): BaseSendNotification() {
    override val category = NotificationSendCategory.CHAT_MEMBER_ACTION
}