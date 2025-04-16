package ru.mephi.chatservice.models.dto.kafka

import ru.mephi.chatservice.models.ChatMemberAction
import ru.mephi.chatservice.models.dto.rest.MemberInfo
import java.util.*

data class ChatMemberOutgoingMessage (
    val action: ChatMemberAction,
    val userId: UUID,
    val chatId: UUID,
    val memberId: UUID,
    val member: MemberInfo?
)