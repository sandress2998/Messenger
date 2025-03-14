package ru.mephi.websocket.model.dto.kafka.send

data class UserStatusChangeEvent (
    val email: String,
    val status: String
)