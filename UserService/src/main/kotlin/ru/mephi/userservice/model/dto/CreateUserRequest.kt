package ru.mephi.userservice.model.dto

import java.util.*

data class CreateUserRequest (
    val id: UUID,
    val username: String,
    val tag: String,
    val email: String,
    val showEmail: Boolean
)