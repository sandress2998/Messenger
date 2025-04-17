package ru.mephi.presence.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    fun consumerConfig(): Map<String, Any> {
        return hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
    }

    @Bean
    fun messageConsumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(
            consumerConfig(),
            StringDeserializer(),
            StringDeserializer()
        )
    }

    @Bean
    fun messageKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = messageConsumerFactory()
        return factory
    }
}


/*
@Configuration
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    fun consumerConfig(): Map<String, Any> {
        return hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
    }

    @Bean
    fun messageConsumerFactory(): ConsumerFactory<String, UserStatusChangeEvent> {
        return DefaultKafkaConsumerFactory(
            consumerConfig(),
            StringDeserializer(),
            JsonDeserializer(UserStatusChangeEvent::class.java)
        )
    }

    @Bean
    fun messageKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserStatusChangeEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserStatusChangeEvent>()
        factory.consumerFactory = messageConsumerFactory()
        return factory
    }
}
 */