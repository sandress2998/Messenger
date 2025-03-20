package ru.mephi.messagehandler.models.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "messages")
data class Message(
    @Id
    val id: UUID? = null,
    val senderId: UUID,
    val chatId: UUID,
    val text: String,
    val timestamp: Date
)