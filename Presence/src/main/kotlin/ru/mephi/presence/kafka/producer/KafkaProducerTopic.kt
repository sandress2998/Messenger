package ru.mephi.presence.kafka.producer

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaProducerTopic {
    @Bean
    fun fromPresenceToWsTopic(): NewTopic {
        return TopicBuilder
            .name("activity-from-presence-to-ws")
            .build()
    }
}