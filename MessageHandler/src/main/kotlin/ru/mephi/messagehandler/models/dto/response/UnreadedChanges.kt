package ru.mephi.messagehandler.models.dto.response

import ru.mephi.messagehandler.models.entity.Message
import java.util.*

class UnreadChanges(
    val newMessages: MutableList<Message>,
    val editedMessages: MutableList<Message>,
    val deletedId:  MutableList<UUID>
): SuccessResult()