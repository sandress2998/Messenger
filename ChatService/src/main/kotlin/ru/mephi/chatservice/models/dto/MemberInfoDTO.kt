package ru.mephi.chatservice.models.dto

import ru.mephi.chatservice.models.ChatRole
import java.util.*

data class MemberInfoDTO (
    val userId: UUID = UUID.randomUUID(),
    val role: ChatRole = ChatRole.MEMBER
)