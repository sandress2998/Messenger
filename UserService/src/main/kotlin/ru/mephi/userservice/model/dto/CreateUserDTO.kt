package ru.mephi.userservice.model.dto

import java.util.*

data class CreateUserDTO (
    val id: UUID,
    val username: String,
    val email: String
)