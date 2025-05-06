package ru.mephi.websocket.model.dto.kafka.receive

import ru.mephi.websocket.shared.data.UserInfo
import ru.mephi.websocket.shared.enums.UserAction
import java.util.*

data class UserActionIngoingMessage (
    val userId: UUID,
    val action: UserAction,
    val userInfo: UserInfo?
)