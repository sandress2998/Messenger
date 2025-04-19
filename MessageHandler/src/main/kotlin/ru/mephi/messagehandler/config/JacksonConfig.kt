package ru.mephi.messagehandler.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.mephi.messagehandler.webclient.dto.ChatId
import ru.mephi.messagehandler.webclient.dto.ChatIdDeserializer
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .addModule(createJavaTimeModule())
            .addModule(SimpleModule().apply {
                addDeserializer(ChatId::class.java, ChatIdDeserializer())
            })
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()
    }

    private fun createJavaTimeModule(): JavaTimeModule {
        val module = JavaTimeModule()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC)

        module.addSerializer(Instant::class.java, object : JsonSerializer<Instant>() {
            override fun serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider) {
                gen.writeString(formatter.format(value))
            }
        })

        module.addDeserializer(Instant::class.java, object : JsonDeserializer<Instant>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
                return try {
                    Instant.from(formatter.parse(p.text))
                } catch (e: DateTimeParseException) {
                    throw JsonParseException(p, "Failed to parse Instant", e)
                }
            }
        })

        return module
    }
}