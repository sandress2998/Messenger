package ru.mephi.messagehandler.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.mephi.messagehandler.models.dto.MessageCreateDTO
import ru.mephi.messagehandler.models.dto.MessageUpdateDTO
import ru.mephi.messagehandler.service.KafkaProducerService
import ru.mephi.messagehandler.service.MessageService
import java.util.*

@RestController
@RequestMapping("/test")
class TestMessageController (
    private val kafkaProducer: KafkaProducerService
){
    @PostMapping("/kafka")
    fun postKafkaMessage(
        @RequestBody message: MessageCreateDTO
    ) : Mono<MessageCreateDTO> {
        kafkaProducer.sendCreateMessage(message)
        return Mono.just(message)
    }

    @PatchMapping("/kafka/{messageId}")
    fun patchKafkaMessage(
        @PathVariable("messageId") messageId : UUID,
        @RequestBody message: MessageUpdateDTO
    ) : Mono<MessageUpdateDTO> {
        kafkaProducer.sendUpdateMessage(message.copy(id = messageId))
        return Mono.just(message)
    }

    @DeleteMapping("/kafka/{messageId}")
    fun deleteKafkaMessage(
        @PathVariable("messageId") messageId : UUID,
    ) : Mono<Void> {
        kafkaProducer.sendDeleteMessage(messageId)
        return Mono.empty()
    }
}
