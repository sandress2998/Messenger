package ru.mephi.chatservice.model.dto.kafka

import ru.mephi.chatservice.model.ChatMemberAction
import ru.mephi.chatservice.model.dto.rest.MemberInfo
import java.util.*

data class ChatMemberOutgoingMessage (
    val action: ChatMemberAction,
    val userId: UUID,
    val chatId: UUID,
    val memberId: UUID,
    val member: MemberInfo?
)