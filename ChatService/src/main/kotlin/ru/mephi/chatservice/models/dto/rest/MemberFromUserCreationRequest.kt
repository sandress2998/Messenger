package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ChatRole

data class MemberFromUserCreationRequest (
    val tag: String,
    val role: ChatRole
)