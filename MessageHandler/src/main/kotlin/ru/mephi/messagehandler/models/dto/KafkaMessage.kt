package ru.mephi.messagehandler.models.dto

import ru.mephi.messagehandler.models.MessageType
import ru.mephi.messagehandler.models.entity.Message


data class KafkaMessage (
    val messageType : MessageType,
    val message : Message
)
