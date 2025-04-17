package ru.mephi.presence.model.service

import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.kafka.ActivityChangeEvent

interface StatusMessaging {
    fun handleChatActivityMessage(message: ActivityChangeEvent): Mono<Void>
}