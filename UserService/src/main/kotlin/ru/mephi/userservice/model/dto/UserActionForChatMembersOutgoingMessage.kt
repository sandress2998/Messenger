package ru.mephi.userservice.model.dto

import ru.mephi.userservice.model.UserAction
import java.util.*

data class UserActionForChatMembersOutgoingMessage (
    val userId: UUID,
    val action: UserAction
)