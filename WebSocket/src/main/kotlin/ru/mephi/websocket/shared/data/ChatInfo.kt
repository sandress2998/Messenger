package ru.mephi.websocket.shared.data

import java.util.*

data class ChatInfo (
    val chatId: UUID,
    val name: String,
    val membersQuantity: Int
)