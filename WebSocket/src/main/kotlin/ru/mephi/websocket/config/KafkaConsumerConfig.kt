package ru.mephi.websocket.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory


@Configuration
@EnableKafka
class KafkaConsumerConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    fun consumerConfig(): Map<String, Any> {
        return hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
    }

    @Bean
    fun activityMessageConsumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(
            consumerConfig(),
            StringDeserializer(),
            StringDeserializer()
        )
    }

    @Bean
    fun activityMessageListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = activityMessageConsumerFactory()
        return factory
    }
}

/*
@Configuration
class KafkaActivityMessageConsumerConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    fun consumerConfig(): Map<String, Any> {
        return hashMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
    }

    @Bean
    fun activityMessageConsumerFactory(): ConsumerFactory<String, UserStatusChangeBroadcast> {
        return DefaultKafkaConsumerFactory(
            consumerConfig(),
            StringDeserializer(),
            JsonDeserializer(UserStatusChangeBroadcast::class.java)
        )
    }

    @Bean
    fun activityMessageListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserStatusChangeBroadcast> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserStatusChangeBroadcast>()
        factory.consumerFactory = activityMessageConsumerFactory()
        return factory
    }
}
 */