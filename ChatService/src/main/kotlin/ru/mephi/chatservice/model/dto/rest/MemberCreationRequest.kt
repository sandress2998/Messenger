package ru.mephi.chatservice.model.dto.rest

import ru.mephi.chatservice.model.ChatRole
import java.util.*

data class MemberCreationRequest (
    val someMemberId: UUID,
    val role: ChatRole
)