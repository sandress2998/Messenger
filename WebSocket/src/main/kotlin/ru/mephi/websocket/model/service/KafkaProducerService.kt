package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.send.ActivityChangeOutgoingMessage

interface KafkaProducerService {
    fun sendActivityStatusMessage(message: ActivityChangeOutgoingMessage): Mono<Void>
}