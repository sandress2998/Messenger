package ru.mephi.authentication.init

import jakarta.annotation.PostConstruct
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(private val databaseClient: DatabaseClient) {

    @PostConstruct
    fun init() {
        databaseClient.sql(
            """
            CREATE TABLE IF NOT EXISTS passwords (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                email VARCHAR(255) NOT NULL UNIQUE,
                hashed_password VARCHAR(255) NOT NULL
            );
            """
        ).fetch().rowsUpdated().block()
    }
}