package ru.mephi.websocket.model.dto.kafka.send

data class ChatActivityChangeOutgoingMessage (
    val chatID: String,
    val email: String, // тот пользователь, у которого изменился статус
    val status: String
)