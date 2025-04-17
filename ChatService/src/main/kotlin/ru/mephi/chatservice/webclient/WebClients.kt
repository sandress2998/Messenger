package ru.mephi.chatservice.webclient

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClients {
    @Bean
    fun messageHandlerServiceWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://message-handler-service:8091")
            .build()
    }

    @Bean
    fun presenceServiceWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://presence-service:8093")
            .build()
    }
}