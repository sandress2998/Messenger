package ru.mephi.websocket.model.dto.kafka.receive

data class UserStatusChangeBroadcast (
    val email: String,
    val status: String,
    val receivers: List<String>
)