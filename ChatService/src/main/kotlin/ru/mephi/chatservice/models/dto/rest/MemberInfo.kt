package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ActivityStatus
import ru.mephi.chatservice.models.ChatRole
import java.util.*

class MemberInfo (
    val memberId: UUID,
    val username: String,
    val role: ChatRole,
    val activity: ActivityStatus
): SuccessResult()