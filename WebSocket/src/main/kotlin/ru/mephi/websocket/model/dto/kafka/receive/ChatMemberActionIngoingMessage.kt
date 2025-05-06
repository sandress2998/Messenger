package ru.mephi.websocket.model.dto.kafka.receive

import ru.mephi.websocket.shared.data.MemberInfo
import ru.mephi.websocket.shared.enums.ChatMemberAction
import java.util.*

data class ChatMemberActionIngoingMessage (
    val action: ChatMemberAction,
    val userId: UUID,
    val chatId: UUID,
    val memberId: UUID,
    val member: MemberInfo?
)