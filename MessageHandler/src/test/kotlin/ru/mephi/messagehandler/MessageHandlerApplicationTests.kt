package ru.mephi.messagehandler

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

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
