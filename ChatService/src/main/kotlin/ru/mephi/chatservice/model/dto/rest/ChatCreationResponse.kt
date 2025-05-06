package ru.mephi.chatservice.model.dto.rest

import java.util.*

class ChatCreationResponse (
    val chatId: UUID,
    val memberId: UUID
): SuccessResult()