package ru.mephi.websocket.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import ru.mephi.websocket.handler.SimpleWebSocketHandler
import ru.mephi.websocket.model.service.KafkaProducerService
import ru.mephi.websocket.model.service.SessionService

@Configuration
class WebSocketConfig(
    private val sessionService: SessionService,
    private val kafkaProducerService: KafkaProducerService,
    private val registry: MeterRegistry
) {
    @Bean
    fun webSocketHandler(): WebSocketHandler {
        return SimpleWebSocketHandler(sessionService, kafkaProducerService, registry)
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