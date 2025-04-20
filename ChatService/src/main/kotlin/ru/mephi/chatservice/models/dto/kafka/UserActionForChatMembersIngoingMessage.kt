package ru.mephi.chatservice.models.dto.kafka

import java.util.*

data class UserActionForChatMembersIngoingMessage(
    val userId: UUID,
    val action: UserAction
)

enum class UserAction {
    DELETED, UPDATED
}