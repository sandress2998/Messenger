package ru.mephi.userservice.init

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopics {
    @Bean
    fun userActionTopic() : NewTopic {
        return TopicBuilder
            .name("user-action")
            .build()
    }

    @Bean
    fun userActionForChatMembersTopic() : NewTopic {
        return TopicBuilder
            .name("user-action-for-chat-members")
            .build()
    }
}