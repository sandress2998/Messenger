package ru.mephi.chatservice.models.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.mephi.chatservice.models.ChatRole
import java.util.*


@Table(name = "chats_members")
data class ChatMember(
    @Id
    val id: UUID? = null,

    @Column(value = "chat_id")
    val chatId: UUID,

    @Column(value = "user_id")
    val userId: UUID,

    val role: ChatRole = ChatRole.MEMBER
)