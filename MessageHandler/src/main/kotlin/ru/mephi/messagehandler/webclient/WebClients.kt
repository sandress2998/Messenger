package ru.mephi.messagehandler.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClients {
    @Bean
    fun chatServiceWebClient(objectMapper: ObjectMapper): WebClient {
        return WebClient.builder()
            .baseUrl("http://chat-service:8082") // Базовый URL для сервиса пользователей
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(objectMapper)
                )
            }
            .build()
    }
}