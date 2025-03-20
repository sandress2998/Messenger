package ru.mephi.messagehandler.models.dto

import java.util.*

data class MessageCreateDTO(
    val chatId : UUID,
    val senderId: UUID,
    val text: String
)

