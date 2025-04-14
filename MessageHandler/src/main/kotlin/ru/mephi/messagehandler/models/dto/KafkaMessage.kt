package ru.mephi.messagehandler.models.dto

import ru.mephi.messagehandler.models.MessageAction
import ru.mephi.messagehandler.models.entity.Message

data class KafkaMessage (
    val messageType : MessageAction,
    val message : Message
)
