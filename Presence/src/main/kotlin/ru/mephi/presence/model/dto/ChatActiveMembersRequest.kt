package ru.mephi.presence.model.dto

data class ChatActiveMembersRequest (
    val chatID: String,
    val requesting: String,
)