package ru.mephi.messagehandler.webclient.dto

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import ru.mephi.messagehandler.models.dto.response.SuccessResult
import java.util.*

class ChatId (
    val chatId: UUID
): SuccessResult()

class ChatIdDeserializer : JsonDeserializer<ChatId>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ChatId {
        return when (p.currentToken) {
            // Если сервер возвращает простой UUID
            JsonToken.VALUE_STRING -> ChatId(UUID.fromString(p.text))

            // Если сервер возвращает объект {"chatId": "..."}
            JsonToken.START_OBJECT -> {
                val node = p.codec.readTree<JsonNode>(p)
                val uuidText = node["chatId"].asText()
                ChatId(UUID.fromString(uuidText))
            }

            else -> throw JsonParseException(p, "Unexpected JSON format for ChatId")
        }
    }
}