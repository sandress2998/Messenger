package ru.mephi.websocket.model.dto.websocket.receive

class ChatActivityChangeIngoingNotification(
    val chatID: String,
    val status: String
): BaseReceiveNotification() {
    override val category = "activity_status"
}