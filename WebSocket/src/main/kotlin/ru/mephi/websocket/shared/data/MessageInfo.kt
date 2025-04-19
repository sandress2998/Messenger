package ru.mephi.websocket.shared.data

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.mephi.websocket.shared.enums.MessageStatus
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "action",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NewMessageInfo::class, name = "NEW"),
    JsonSubTypes.Type(value = UpdatedMessageInfo::class, name = "UPDATED")
)
abstract class MessageInfo

data class NewMessageInfo (
    val text: String,
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    // в свое оправдание: целый день (~12 часов) пыталась без ошибок десериализовать timestamp в Instant, не получилось
    val timestamp: String,
    val status: MessageStatus = MessageStatus.NOT_VIEWED,
    val viewedBy: MutableList<UUID> = mutableListOf()
): MessageInfo()

data class UpdatedMessageInfo (
    val text: String
): MessageInfo()

