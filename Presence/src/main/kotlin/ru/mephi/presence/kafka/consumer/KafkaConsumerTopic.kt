package ru.mephi.presence.kafka.consumer

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConsumerTopic {
    @Bean
    fun fromWsToPresenceTopic(): NewTopic {
        return TopicBuilder
            .name("activity-from-ws-to-presence")
            .build()
    }
}
