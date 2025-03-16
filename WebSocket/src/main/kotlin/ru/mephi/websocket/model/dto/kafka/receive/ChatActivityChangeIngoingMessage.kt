package ru.mephi.websocket.model.dto.kafka.receive

data class ChatActivityChangeIngoingMessage (
    val chatID: String,
    val email: String, // пользователь, у которого изменился статус
    val status: String,
    val receiver: String // пользователь, которого надо уведомить об изменении активности другого пользователя
)