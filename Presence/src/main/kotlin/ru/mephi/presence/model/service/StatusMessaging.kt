package ru.mephi.presence.model.service

import reactor.core.publisher.Mono
import ru.mephi.presence.kafka.dto.ChatActivityChangeEvent

interface StatusMessaging {
    fun handleChatActivityMessage(message: ChatActivityChangeEvent): Mono<Void>
}