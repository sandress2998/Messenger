package ru.mephi.userservice.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import ru.mephi.userservice.model.dto.UserActionForChatMembersOutgoingMessage
import ru.mephi.userservice.model.dto.UserActionOutgoingMessage

@Configuration
class KafkaProducerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    // Общая конфигурация Producer
    private fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        )
    }

    // Фабрика для UserActionOutgoingMessage
    @Bean
    fun userActionProducerFactory(): ProducerFactory<String, UserActionOutgoingMessage> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    // Фабрика для UserActionForChatMembersOutgoingMessage
    @Bean
    fun chatMembersProducerFactory(): ProducerFactory<String, UserActionForChatMembersOutgoingMessage> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    // KafkaTemplate для user-action
    @Bean
    fun userActionKafkaTemplate(): KafkaTemplate<String, UserActionOutgoingMessage> {
        return KafkaTemplate(userActionProducerFactory())
    }

    // KafkaTemplate для user-action-for-chat-members
    @Bean
    fun chatMembersKafkaTemplate(): KafkaTemplate<String, UserActionForChatMembersOutgoingMessage> {
        return KafkaTemplate(chatMembersProducerFactory())
    }
}