package ru.mephi.messagehandler.service


import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mephi.messagehandler.models.dto.request.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.request.MessageUpdateDTO
import java.util.*
/*
@Service
@KafkaListener(topics = ["messages-incoming"], groupId = "message-handlers")
class KafkaConsumerService(private val messageService: MessageService) {
    @KafkaHandler
    @Transactional
    fun createMessageProcess(
        @Payload incomingMessage: MessageCreateDTO
    ) {
        messageService.createMessage(incomingMessage)
            .subscribe(
                { result -> println("Message_create processed successfully! : ${result}") }, //Дописать прокидывание в processed
                { error -> println("Message_create processing error : ${error.message}!") }
            )
    }

    @KafkaHandler
    @Transactional
    fun updateMessageProcessed(
        @Payload incomingMessage: MessageUpdateDTO
    ) {

        messageService.updateMessage(incomingMessage)
            .subscribe(
                { result -> println("Message_create processed successfully! : ${result}") }, //Дописать прокидывание в processed
                { error -> println("Message_create processing error : ${error.message}!") }
            )
    }

    @KafkaHandler
    @Transactional
    fun deleteMessageProcessed(
        @Payload incomingMessage: UUID
    ) {
        messageService.deleteMessageById(incomingMessage)
            .subscribe(
                { result -> println("Message_delete processed successfully! : ${result}") }, //Дописать прокидывание в processed
                { error -> println("Message_delete processing error : ${error.message}!") }
            )
    }
}
 */