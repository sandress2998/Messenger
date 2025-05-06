package ru.mephi.chatservice.model.dto.rest

import ru.mephi.chatservice.model.ChatRole

data class UpdateChatMemberRole(
    val role: ChatRole
)
