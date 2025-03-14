package ru.mephi.gateway.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import ru.mephi.gateway.security.JwtService
import ru.mephi.gateway.security.JwtServiceImpl
import ru.mephi.gateway.security.SecurityProperties
import java.util.HashMap

@Configuration
class WebSocketConfig {

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/ws"] = webSocketHandler()

        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.order = 1
        handlerMapping.urlMap = map
        return handlerMapping
    }

    @Bean
    fun securityProperties(): SecurityProperties {
        return SecurityProperties()
    }

    @Bean
    fun jwtService(): JwtService {
        return JwtServiceImpl(securityProperties())
    }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

    @Bean
    fun webSocketHandler(): WebSocketHandler {
        return ProxyWebSocketHandler(ReactorNettyWebSocketClient(), jwtService())
    }
}