package ru.mephi.websocket.model.service

import org.apache.kafka.common.protocol.types.Field.Bool
import reactor.core.publisher.Mono
import ru.mephi.websocket.dto.kafka.receive.ChatActivityChangeIngoingMessage
import ru.mephi.websocket.dto.kafka.send.ActivityChangeOutgoingMessage
import java.util.*

interface ActivityStatusService {
    // notification to ws-client
    fun sendStatusUpdateNotification(message: ChatActivityChangeIngoingMessage): Mono<Void>

    // message to kafka-topic
    fun sendStatusUpdateMessage(message: ActivityChangeOutgoingMessage): Mono<Void>
}