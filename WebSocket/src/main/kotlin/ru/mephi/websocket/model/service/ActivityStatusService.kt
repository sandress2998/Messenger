package ru.mephi.websocket.model.service

import reactor.core.publisher.Mono
import ru.mephi.websocket.model.dto.kafka.receive.UserStatusChangeBroadcast
import ru.mephi.websocket.model.dto.kafka.send.UserStatusChangeEvent

interface ActivityStatusService {
    // notification to ws-client
    fun sendStatusUpdateNotification(message: UserStatusChangeBroadcast): Mono<Void>

    // message to kafka-topic
    fun sendStatusUpdateMessage(message: UserStatusChangeEvent): Mono<Void>
}