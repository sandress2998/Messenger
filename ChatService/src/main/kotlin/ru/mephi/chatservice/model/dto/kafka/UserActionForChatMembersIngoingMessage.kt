package ru.mephi.chatservice.model.dto.kafka

import java.util.*

data class UserActionForChatMembersIngoingMessage(
    val userId: UUID,
    val action: UserAction
)

enum class UserAction {
    DELETED, UPDATED
}