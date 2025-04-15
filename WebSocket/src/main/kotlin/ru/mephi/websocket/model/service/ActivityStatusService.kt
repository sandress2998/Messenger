package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.kafka.dto.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.kafka.dto.send.ChatActivityChangeOutgoingMessage

interface ActivityStatusService {
    // notification to ws-client
    fun sendStatusUpdateNotification(message: ChatActivityChangeIngoingMessage): Mono<Void>

    // message to kafka-topic
    fun sendStatusUpdateMessage(message: ChatActivityChangeOutgoingMessage): Mono<Void>
}