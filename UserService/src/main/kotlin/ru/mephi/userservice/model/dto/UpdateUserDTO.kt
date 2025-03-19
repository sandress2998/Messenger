package ru.mephi.userservice.model.dto

import java.util.*

data class UpdateUserDTO (
    val id : UUID,
    val username: String,
    val email: String,
)
data class UpdateUserHttpDTO (
    val username: String,
    val email: String,
)