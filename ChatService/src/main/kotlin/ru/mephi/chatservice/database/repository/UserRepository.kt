package ru.mephi.chatservice.database.repository

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

// хоть и есть user-service (микросервис), здесь элементарные операции по работе пользователями
@Repository
class UserRepository(private val databaseClient: DatabaseClient) {

    //@Query("SELECT username FROM users WHERE id = :userId;")
    fun getUsernameById(userId: UUID): Mono<String?> {
        return databaseClient.sql("SELECT username FROM users WHERE id = $1") // Используем $1 вместо :userId
            .bind(0, userId) // Индексация с 0
            .map { row, _ -> row.get("username", String::class.java) }
            .one()
    }

    //@Query("SELECT id FROM users WHERE email = :email;")
    fun getUserIdByEmail(email: String): Mono<UUID?> {
        return databaseClient.sql("SELECT id FROM users WHERE email = $1;") // Используем $1 вместо :email
            .bind(0, email) // Индексация с 0
            .map { row, _ -> row.get("id", UUID::class.java) }
            .one()
    }

    fun getUserIdAndUsernameByTag(tag: String): Mono<Pair<UUID?, String?>> {
        return databaseClient.sql("SELECT id, username FROM users WHERE tag = $1;") // Используем $1 вместо :email
            .bind(0, tag) // Индексация с 0
            .map { row, _ -> Pair(row.get("id", UUID::class.java), row.get("username", String::class.java)) }
            .one()
    }


    /* Это надо убрать, т.к. теперь нельзя достать статус активности через postgres
    fun getUserInfoById(userId: UUID): Mono<UserInfo> {
        return databaseClient.sql("SELECT username, activity FROM users WHERE id = $1;")
            .bind(0, userId)
            .map { row, _ -> UserInfo(
                row.get("username", String::class.java),
                ActivityStatus.valueOf(row.get("activity", String::class.java)!!)
                )
            }
            .one()
    }

    fun getUserInfoByEmail(email: String): Mono<UserInfoExpanded> {
        return databaseClient.sql("SELECT id, username, activity FROM users WHERE email = $1;")
            .bind(0, email)
            .map { row, _ -> UserInfoExpanded(
                row.get("id", UUID::class.java),
                row.get("username", String::class.java),
                ActivityStatus.valueOf(row.get("activity", String::class.java)!!)
            )
            }
            .one()
    }
    */
}