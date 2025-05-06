package ru.mephi.websocket.model.dto.websocket.send

import ru.mephi.websocket.shared.data.UserInfo
import ru.mephi.websocket.shared.enums.UserAction

data class UserActionOutgoingNotification (
    val action: UserAction,
    val userInfo: UserInfo?
): BaseSendNotification() {
    override val category: NotificationSendCategory = NotificationSendCategory.USER_ACTION
}