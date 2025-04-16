package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ChatRole
import java.util.*

data class MemberCreationRequest (
    val someMemberId: UUID,
    val role: ChatRole
)