package ru.mephi.chatservice.model.dto.rest

import ru.mephi.chatservice.model.ChatRole

data class MemberFromUserCreationRequest (
    val tag: String,
    val role: ChatRole
)