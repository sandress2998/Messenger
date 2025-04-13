package ru.mephi.chatservice.models.dto

import ru.mephi.chatservice.models.ActivityStatus

class UserInfo(
    val username: String?,
    val activity: ActivityStatus?
): SuccessResult()