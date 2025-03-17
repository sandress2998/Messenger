package ru.mephi.presence.model.service

import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.ChatActivityChangeEvent

interface StatusMessaging {
    fun handleChatActivityMessage(message: ChatActivityChangeEvent): Mono<Void>
}