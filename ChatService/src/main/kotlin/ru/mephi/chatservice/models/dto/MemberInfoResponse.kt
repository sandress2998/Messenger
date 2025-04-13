package ru.mephi.chatservice.models.dto

import ru.mephi.chatservice.models.ActivityStatus
import ru.mephi.chatservice.models.ChatRole
import java.util.*

class MemberInfoResponse (
    val memberId: UUID,
    val username: String,
    val role: ChatRole,
    val activity: ActivityStatus
): SuccessResult()