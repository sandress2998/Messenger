package ru.mephi.messagehandler.model.dto.rest.response

import ru.mephi.messagehandler.database.entity.Message
import java.util.*

class UnreadChanges(
    val newMessages: List<Message>,
    val editedMessages: List<Message>,
    val deletedId:  List<UUID>,
    val viewedInfo: List<ViewedInfo>
)

data class ViewedInfo(val id: UUID, val membersId: List<UUID>)