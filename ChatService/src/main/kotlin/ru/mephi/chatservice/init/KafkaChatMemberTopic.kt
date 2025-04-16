package ru.mephi.chatservice.init

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaChatMemberTopic {
    @Bean
    fun chatMemberActionTopic(): NewTopic {
        println("Topic chat-member-action CREATED")
        return TopicBuilder
            .name("chat-member-action")
            .build()
    }
}