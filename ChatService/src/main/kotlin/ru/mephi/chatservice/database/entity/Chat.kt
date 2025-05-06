package ru.mephi.chatservice.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "chats")
data class Chat(
    val name: String = "",
    @Id
    val id: UUID? = null,
)
