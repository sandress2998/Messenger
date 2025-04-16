package ru.mephi.chatservice.models.dto.kafka

import ru.mephi.chatservice.models.ChatAction
import ru.mephi.chatservice.models.dto.rest.ChatInfo
import java.util.*

data class ChatOutgoingMessage(
    val action: ChatAction,
    val userId: UUID,
    val chatId: UUID,
    val updatedChat: ChatInfo?
)
