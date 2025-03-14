package ru.mephi.websocket.model.dto.websocket.send


// ПЕРЕДЕЛАТЬ: ВМЕСТ EMAIL ПОЛЬЗОВАТЕЛЯ НУЖНО ИСПОЛЬЗОВАТЬ НОМЕР (ID) ПОЛЬЗОВАТЕЛЯ
data class UserStatusChangeNotification (
    val email: String,
    val status: String
): BaseNotification() {
    override val category = "activity_status"
}