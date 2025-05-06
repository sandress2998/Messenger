package ru.mephi.websocket.model.dto.kafka.receive

import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.mephi.websocket.shared.data.MessageInfo
import ru.mephi.websocket.shared.enums.MessageAction
import java.util.*

data class MessageActionIngoingMessage(
    val userId: UUID,
    val action: MessageAction,
    val messageId: UUID,
    val chatId: UUID,
    val memberId: UUID,

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "action"
    )
    val messageInfo: MessageInfo?
)
