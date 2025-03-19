package ru.mephi.chatservice.init

import jakarta.annotation.PostConstruct
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class ChatMembersDatabaseInitializer(private val databaseClient: DatabaseClient) {

    @PostConstruct
    fun initialize() {
        databaseClient.sql(
            """
                CREATE TABLE IF NOT EXISTS  chats_members (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                chat_id UUID NOT NULL REFERENCES chats(id),
                user_id UUID NOT NULL,
                UNIQUE (chat_id, user_id),
                role VARCHAR(255) NOT NULL
            )
            """
        ).fetch().rowsUpdated().subscribe()
    }
}