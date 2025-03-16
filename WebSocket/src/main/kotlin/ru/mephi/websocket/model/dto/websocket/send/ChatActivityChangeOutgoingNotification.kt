package ru.mephi.websocket.model.dto.websocket.send


// ПЕРЕДЕЛАТЬ: ВМЕСТ EMAIL ПОЛЬЗОВАТЕЛЯ НУЖНО ИСПОЛЬЗОВАТЬ НОМЕР (ID) ПОЛЬЗОВАТЕЛЯ
data class ChatActivityChangeOutgoingNotification (
    val chatID: String,
    val email: String, // пользователь, который поменял статус
    val status: String
): BaseSendNotification() {
    override val category = "activity_status"
}