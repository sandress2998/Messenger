package ru.mephi.chatservice.model.dto.kafka

import ru.mephi.chatservice.model.ChatAction
import ru.mephi.chatservice.model.dto.rest.ChatInfo
import java.util.*

data class ChatOutgoingMessage(
    val action: ChatAction,
    val userId: UUID,
    val chatId: UUID,
    val updatedChat: ChatInfo?
)
