package ru.mephi.chatservice.models.dto

// для kafka
data class ChatActivityChangeBroadcast (
    val userId: String,
    val status: String
)