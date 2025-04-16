package ru.mephi.chatservice.models.dto.rest

import ru.mephi.chatservice.models.ChatRole

class UserRoleInChat (
    val role: ChatRole
): SuccessResult()