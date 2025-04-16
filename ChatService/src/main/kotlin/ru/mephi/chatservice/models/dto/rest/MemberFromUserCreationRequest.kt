package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ChatRole

data class MemberFromUserCreationRequest (
    val email: String,
    val role: ChatRole
)