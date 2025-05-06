package ru.mephi.messagehandler.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant
import java.util.*

@Document(collection = "messages")
@CompoundIndex(def = "{'chatId': 1, 'timestamp': 1}")
data class Message (
    @Field(targetType = FieldType.BINARY)
    val memberId: UUID,

    @Field(targetType = FieldType.BINARY)
    val chatId: UUID,

    @TextIndexed
    val text: String,

    val timestamp: Instant,

    @Id
    @Field(targetType = FieldType.BINARY)
    val id: UUID,

    val status: MessageStatus = MessageStatus.NOT_VIEWED,

    val viewedBy: MutableList<UUID> = mutableListOf()
)

enum class MessageStatus {
    VIEWED, NOT_VIEWED
}


/*

@Document(collection = "{chatId}")
@CompoundIndex(def = "{'timestamp': 1}")
data class Message(
    @Id
    val id: UUID? = null,
    val senderId: UUID,
    val chatId: UUID,
    val text: String,
    val timestamp: Instant,
    val status: MessageStatus = MessageStatus.NOT_VIEWED,
    val viewedBy: MutableList<UUID> = mutableListOf()
)

enum class MessageStatus {
    VIEWED, NOT_VIEWED
}
 */