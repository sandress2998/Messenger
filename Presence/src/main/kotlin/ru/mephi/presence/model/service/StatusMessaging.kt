package ru.mephi.presence.model.service

import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.UserStatusChangeEvent

interface StatusMessaging {
    fun handleActivityMessage(message: UserStatusChangeEvent): Mono<Void>
}