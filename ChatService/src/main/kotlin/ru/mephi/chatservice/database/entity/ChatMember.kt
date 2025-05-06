package ru.mephi.chatservice.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.mephi.chatservice.model.ChatRole
import java.util.*


@Table(name = "chats_members")
data class ChatMember(
    @Column(value = "chat_id")
    val chatId: UUID,

    @Column(value = "user_id")
    val userId: UUID,

    val role: ChatRole = ChatRole.MEMBER,

    @Id
    val id: UUID? = null
)