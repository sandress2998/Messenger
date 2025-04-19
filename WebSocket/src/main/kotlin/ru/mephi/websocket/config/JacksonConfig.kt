package ru.mephi.websocket.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.util.*

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper = Jackson2ObjectMapperBuilder()
        .modules(
            JavaTimeModule(), // Для работы с Instant
            KotlinModule.Builder().build() // Для Kotlin-классов
        )
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .timeZone(TimeZone.getTimeZone("UTC"))
        .build()
}
