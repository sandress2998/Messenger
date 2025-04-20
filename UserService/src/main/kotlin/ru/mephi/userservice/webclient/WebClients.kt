package ru.mephi.userservice.webclient

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class WebClients {
    @Bean
    fun authServiceWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://authentication-service:8090")
            .build()
    }

    @Bean
    fun presenceServiceWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://presence-service:8093") //
            .build()
    }
}