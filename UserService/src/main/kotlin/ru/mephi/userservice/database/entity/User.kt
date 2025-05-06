package ru.mephi.userservice.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "users")
data class User (
    @Id
    val id : UUID,
    val username : String,
    val tag: String,
    val email : String,
    val showEmail: Boolean
)
