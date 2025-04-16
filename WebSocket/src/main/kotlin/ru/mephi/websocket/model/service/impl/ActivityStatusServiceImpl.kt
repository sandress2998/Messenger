package ru.mephi.websocket.model.service.impl

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.dto.kafka.send.ActivityChangeOutgoingMessage
import ru.mephi.websocket.model.mapper.Mapper
import ru.mephi.websocket.model.service.ActivityStatusService
import ru.mephi.websocket.model.service.SessionService

// По идее осталось реализовать только это

@Service
class ActivityStatusServiceImpl (
    private val sessionService: SessionService,
    private val mapper: Mapper,
    private val messageKafkaTemplate: KafkaTemplate<String, ActivityChangeOutgoingMessage>
): ActivityStatusService {
    override fun sendStatusUpdateNotification(message: ChatActivityChangeIngoingMessage): Mono<Void> {
        return sessionService.sendNotification(message.userId, mapper.activityMessageAsNotification(message))
    }

    override fun sendStatusUpdateMessage(message: ActivityChangeOutgoingMessage): Mono<Void> {
        return Mono.fromFuture(
            messageKafkaTemplate.send("activity-status-change", message)
        ).then()
    }
}