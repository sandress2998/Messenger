package ru.mephi.messagehandler.init.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {
    @Bean
    fun messageActionTopic() : NewTopic {
        return TopicBuilder
            .name("message-action")
            .build()
    }
}