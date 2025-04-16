package ru.mephi.chatservice.init

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaChatTopic {
    @Bean
    fun chatActionTopic(): NewTopic {
        println("Topic chat-action CREATED")
        return TopicBuilder
            .name("chat-action")
            .build()
    }
}