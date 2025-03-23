package ru.mephi.authentication.webclient

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class WebClients {
    @Bean
    fun userServiceWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://user-service:8083") // Базовый URL для сервиса пользователей
            .build()
    }
}