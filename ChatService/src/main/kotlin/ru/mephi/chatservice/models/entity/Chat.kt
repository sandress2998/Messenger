package ru.mephi.chatservice.models.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "chats")
data class Chat(
    @Id
    val id: UUID? = null,
    val name: String = "",
)
