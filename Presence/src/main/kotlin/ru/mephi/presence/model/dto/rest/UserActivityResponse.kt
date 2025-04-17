package ru.mephi.presence.model.dto.rest

import ru.mephi.presence.model.ActivityStatus

data class UserActivityResponse (
    val status: ActivityStatus
)