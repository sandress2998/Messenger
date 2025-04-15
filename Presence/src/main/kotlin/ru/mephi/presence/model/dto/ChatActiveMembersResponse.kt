package ru.mephi.presence.model.dto

import java.util.*

data class ChatActiveMembersResponse (
    val chatId: UUID,
    val activeMembers: List<UUID>
)