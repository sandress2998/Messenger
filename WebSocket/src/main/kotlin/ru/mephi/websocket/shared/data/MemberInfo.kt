package ru.mephi.websocket.shared.data

import ru.mephi.websocket.shared.enums.ActivityStatus
import ru.mephi.websocket.shared.enums.ChatRole
import java.util.*

data class MemberInfo (
    val username: String,
    val role: ChatRole,
    val activity: ActivityStatus? = null
)