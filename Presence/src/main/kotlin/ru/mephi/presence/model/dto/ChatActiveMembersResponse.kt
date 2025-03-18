package ru.mephi.presence.model.dto

data class ChatActiveMembersResponse (
    val chatID: String,
    val activeMembers: List<String>
)