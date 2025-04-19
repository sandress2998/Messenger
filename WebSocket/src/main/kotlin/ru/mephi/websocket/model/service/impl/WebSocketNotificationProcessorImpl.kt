package ru.mephi.websocket.model.service.impl

/*
@Service
class WebSocketNotificationProcessorImpl(
    private val activityStatusService: ActivityStatusService,
    private val mapper: Mapper
): WebSocketNotificationProcessor {
    override fun processActivityStatusNotification(
        notification: ChatActivityChangeIngoingNotification,
        receiver: UUID
    ): Mono<Void> {
        println("Десериализовано в ChatActivityChangeIngoingNotification: " +
                "chatID: ${notification.chatId}, status: ${notification.status}, receiver: $receiver")
        val outgoingMessage = mapper.activityNotificationAsMessage(notification, receiver)
        return activityStatusService.sendStatusUpdateMessage(outgoingMessage)
    }
}

 */