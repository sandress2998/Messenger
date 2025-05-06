package ru.mephi.messagehandler.models.dto.rest.response

import ru.mephi.messagehandler.database.entity.Message
import java.util.*

class UnreadChanges(
    val newMessages: MutableList<Message>,
    val editedMessages: MutableList<Message>,
    val deletedId:  MutableList<UUID>
): SuccessResult()