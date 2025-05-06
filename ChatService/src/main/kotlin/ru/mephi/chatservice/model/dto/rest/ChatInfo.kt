package ru.mephi.chatservice.model.dto.rest

import java.util.*

class ChatInfo (
    val chatId: UUID? = null,
    val name: String,
    val membersQuantity: Int
): SuccessResult()