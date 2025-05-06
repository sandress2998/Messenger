package ru.mephi.chatservice.model.dto.rest

import ru.mephi.chatservice.model.ActivityStatus
import ru.mephi.chatservice.model.ChatRole
import java.util.*

data class MemberInfo (
    val memberId: UUID? = null,
    val username: String,
    val role: ChatRole,
    val activity: ActivityStatus? = null
): SuccessResult()