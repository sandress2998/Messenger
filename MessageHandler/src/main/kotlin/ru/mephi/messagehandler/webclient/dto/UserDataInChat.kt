package ru.mephi.messagehandler.webclient.dto

import ru.mephi.messagehandler.models.UserStatusInChat
import ru.mephi.messagehandler.models.dto.response.SuccessResult

class UserDataInChat(
    val role: UserStatusInChat
): SuccessResult()

