package ru.mephi.userservice.model.dto

import java.util.*

data class UserDetails (
    val id : UUID,
    val username: String,
)

data class UserPersonalDetails (
    val id : UUID,
    val username: String,
    val email: String
)