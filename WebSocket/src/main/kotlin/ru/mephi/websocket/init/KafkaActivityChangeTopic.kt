package ru.mephi.websocket.init

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaActivityChangeTopic {
    @Bean
    fun activityStatusChangeTopic(): NewTopic {
        return TopicBuilder
            .name("activity-status-change")
            .build()
    }
}