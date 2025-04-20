package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ActivityStatus
import ru.mephi.chatservice.models.ChatRole
import java.util.*

data class MemberInfo (
    val memberId: UUID? = null,
    val username: String,
    val role: ChatRole,
    val activity: ActivityStatus? = null
): SuccessResult()