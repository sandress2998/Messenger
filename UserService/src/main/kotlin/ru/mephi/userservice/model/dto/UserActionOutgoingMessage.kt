package ru.mephi.userservice.model.dto

import ru.mephi.userservice.model.UserAction
import java.util.*

data class UserActionOutgoingMessage (
    val userId: UUID,
    val action: UserAction,
    val userInfo: UserInfo?
)