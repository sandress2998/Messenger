package ru.mephi.presence.model.dto

data class ChatActivityChangeBroadcast (
    val chatID: String,
    val email: String,
    val status: String,
    val receiver: String
)