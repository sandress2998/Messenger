package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ChatRole

data class UpdateChatMemberRole(
    val role: ChatRole
)
