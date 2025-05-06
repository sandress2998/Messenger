package ru.mephi.messagehandler.models.dto.kafka

import ru.mephi.messagehandler.database.entity.Message

data class UpdatedMessageInfo(
    val text: String
): MessageInfo() {
    companion object {
        fun messageAsNotification(message: Message): UpdatedMessageInfo {
            return UpdatedMessageInfo(
                text = message.text
            )
        }
    }
}
