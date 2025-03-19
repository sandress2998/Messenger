package ru.mephi.userservice.init

import jakarta.annotation.PostConstruct
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component


@Component
class UserDatabaseInitializer(private val databaseClient: DatabaseClient) {

    @PostConstruct
    fun initialize() {
        databaseClient.sql(
            """
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                username VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE
            )
            """
        ).fetch().rowsUpdated().subscribe()
    }
}
