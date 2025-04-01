package ru.mephi.chatservice.models.dto

import java.util.*

class ChatCreationResponse (
    val chatId: UUID,
    val memberId: UUID
): SuccessResult()