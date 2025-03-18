package ru.mephi.presence.model.dto

data class ChatActivityChangeEvent (
    val chatID: String,
    val email: String,
    val status: String
)