package ru.mephi.chatservice.init

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaActivityTopic {
    @Bean
    fun activityChangeNotificationToMembersTopic(): NewTopic {
        println("Topic activity-status-notification-to-members CREATED")
        return TopicBuilder
            .name("activity-status-notification-to-members")
            .build()
    }
}