package ru.mephi.chatservice.models.dto.rest

import java.util.*

class ChatInfo (
    val chatId: UUID? = null,
    val name: String,
    val membersQuantity: Int
): SuccessResult()