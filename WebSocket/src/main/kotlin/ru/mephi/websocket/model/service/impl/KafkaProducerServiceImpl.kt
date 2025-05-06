package ru.mephi.websocket.model.service.impl

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.kafka.send.ActivityChangeOutgoingMessage
import ru.mephi.websocket.model.service.KafkaProducerService

@Service
class KafkaProducerServiceImpl (
    private val messageKafkaTemplate: KafkaTemplate<String, ActivityChangeOutgoingMessage>
): KafkaProducerService {
    override fun sendActivityStatusMessage(message: ActivityChangeOutgoingMessage): Mono<Void> {
        return Mono.fromFuture(
            messageKafkaTemplate.send("activity-status-change", message)
        ).then()
    }
}