package ru.mephi.chatservice.models.dto

import java.util.*

class ChatInfoResponse (
    val chatId: UUID,
    val memberId: UUID,
    val name: String,
    val membersQuantity: Int
): SuccessResult()