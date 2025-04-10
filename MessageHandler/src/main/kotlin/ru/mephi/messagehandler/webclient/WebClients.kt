package ru.mephi.messagehandler.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
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