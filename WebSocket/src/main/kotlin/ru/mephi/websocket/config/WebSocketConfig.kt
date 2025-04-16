package ru.mephi.websocket.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import ru.mephi.websocket.handler.SimpleWebSocketHandler
import ru.mephi.websocket.model.service.ActivityStatusService
import ru.mephi.websocket.model.service.SessionService
import ru.mephi.websocket.model.service.WebSocketNotificationProcessor

@Configuration
class WebSocketConfig(
    private val sessionService: SessionService,
    private val activityStatusService: ActivityStatusService,
    private val webSocketNotificationProcessor: WebSocketNotificationProcessor
) {
    @Bean
    fun webSocketHandler(): WebSocketHandler {
        return SimpleWebSocketHandler(sessionService, activityStatusService, webSocketNotificationProcessor)
    }

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/ws"] = webSocketHandler() // Регистрируем обработчик для пути /ws

        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.order = 1
        handlerMapping.urlMap = map
        return handlerMapping
    }
}