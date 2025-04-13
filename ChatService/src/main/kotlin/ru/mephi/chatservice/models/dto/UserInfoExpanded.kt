package ru.mephi.chatservice.models.dto

import ru.mephi.chatservice.models.ActivityStatus
import java.util.*

class UserInfoExpanded(
    val id: UUID?,
    val username: String?,
    val activity: ActivityStatus?
): SuccessResult()