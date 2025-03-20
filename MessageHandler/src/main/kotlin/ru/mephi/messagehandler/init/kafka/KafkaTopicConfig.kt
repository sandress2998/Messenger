package ru.mephi.messagehandler.init.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {
    @Bean
    fun messageIncomingTopic() : NewTopic {
        println("Topic incoming message CREATED")
        return TopicBuilder
            .name("messages-incoming")
            .build()
    }

    @Bean
    fun messageProcessedTopic() : NewTopic {
        return TopicBuilder
            .name("messages-processed")
            .build()
    }
}