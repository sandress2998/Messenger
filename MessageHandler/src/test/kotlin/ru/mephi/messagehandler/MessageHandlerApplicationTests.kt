package ru.mephi.messagehandler

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.mephi.messagehandler.models.MessageAction
import ru.mephi.messagehandler.models.dto.kafka.MessageActionOutgoingMessage
import ru.mephi.messagehandler.models.dto.kafka.NewMessageInfo
import ru.mephi.messagehandler.database.entity.MessageStatus
import ru.mephi.messagehandler.webclient.dto.ChatId
import ru.mephi.messagehandler.webclient.dto.ChatIdDeserializer
import java.time.Instant
import java.util.*

@SpringBootTest
class MessageHandlerApplicationTests {

    @Test
    fun contextLoads() {
    }
/*
    @Test
    fun testSerialization() {
        val message = MessageActionOutgoingMessage(
            UUID.randomUUID(), MessageAction.NEW, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NewMessageInfo(
                "some text in message", Instant.now(), MessageStatus.NOT_VIEWED, mutableListOf()
            )
        )
        val objectMapper = ObjectMapper().apply {
            registerModule(SimpleModule().apply {
                addDeserializer(ChatId::class.java, ChatIdDeserializer())
                JavaTimeModule()
                KotlinModule.Builder().build()
            })
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }

        val json = objectMapper.writeValueAsString(message)
        println(json) // Должен быть корректный JSON с timestamp

        val parsed = objectMapper.readValue(json, MessageActionOutgoingMessage::class.java)
        assertEquals(message, parsed)
    }
 */
}
