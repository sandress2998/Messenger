package ru.mephi.messagehandler.database.entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant
import java.util.*

@Document(collection = "message_sync")
data class MessageReadReceipt (
    @Field(targetType = FieldType.BINARY)
    val userId: UUID,

    @Field(targetType = FieldType.BINARY)
    val chatId: UUID,

    @Field(targetType = FieldType.BINARY)
    val id: UUID,

    @Field("pendingUpdates")
    val pendingUpdates: MessageUpdates = MessageUpdates(), // Изменения, требующие подтверждения

    val lastConfirmedTime: Instant? = null,
)

data class MessageUpdates (
    @Field("edited")
    val edited: MutableList<UUID> = ArrayList<UUID>(),
    @Field("deleted")
    val deleted: MutableList<UUID> = ArrayList<UUID>()
)