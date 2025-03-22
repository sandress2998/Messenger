package ru.mephi.messagehandler.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.mephi.messagehandler.models.dto.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.MessageUpdateDTO
import java.util.*


@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, MessageCreateDTO>
) {
    fun sendCreateMessage(message: MessageCreateDTO) {
        val sendMessage = MessageBuilder
            .withPayload(message)
            .setHeader(KafkaHeaders.TOPIC,"messages-incoming")
            .build()
        kafkaTemplate.executeInTransaction() {
            it.send(sendMessage)
        }
        println(sendMessage)
    }

    fun sendUpdateMessage(message: MessageUpdateDTO) {
        val sendMessage = MessageBuilder
            .withPayload(message)
            .setHeader(KafkaHeaders.TOPIC,"messages-incoming")
            .build()
        kafkaTemplate.executeInTransaction(){
            it.send(sendMessage)
        }
        println(sendMessage)
    }

    fun sendDeleteMessage(messageId : UUID) {
        val sendMessage = MessageBuilder
            .withPayload(messageId)
            .setHeader(KafkaHeaders.TOPIC,"messages-incoming")
            .build()
        kafkaTemplate.executeInTransaction(){
            it.send(sendMessage)
        }
        println(sendMessage)
    }
}
