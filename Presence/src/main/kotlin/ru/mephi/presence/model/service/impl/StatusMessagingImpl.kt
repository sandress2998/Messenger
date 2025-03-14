package ru.mephi.presence.model.service.impl

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.presence.model.dto.UserStatusChangeEvent
import ru.mephi.presence.model.dto.UserStatusChangeBroadcast
import ru.mephi.presence.model.service.StatusMessaging
import ru.mephi.presence.model.service.StatusService


// TODO("Not yet implemented")
@Service
class StatusMessagingImpl(
    private val statusService: StatusService,
    private val messageKafkaTemplate: KafkaTemplate<String, UserStatusChangeBroadcast>
): StatusMessaging {
    override fun handleActivityMessage(message: UserStatusChangeEvent): Mono<Void> {
        val email = message.email
        val status = message.status
        return if (status == "active") {
            statusService.setActive(email)
                .then(sendActivityMessage(email, status))
        } else if (status == "inactive") {
            statusService.setInactive(email)
                .then(sendActivityMessage(email, status))
        } else {
            Mono.error(IllegalStateException("Invalid status message ${message.status}"))
        }

    }

    private fun sendActivityMessage(email: String, status: String): Mono<Void> {
        return statusService.fetchUserTracking(email)
            .flatMap { receivers ->
                val broadcastMessage = UserStatusChangeBroadcast(
                    email, status, receivers
                )
                Mono.fromFuture(messageKafkaTemplate.send("activity-from-presence-to-ws", broadcastMessage))
            }
            .then()
    }
}