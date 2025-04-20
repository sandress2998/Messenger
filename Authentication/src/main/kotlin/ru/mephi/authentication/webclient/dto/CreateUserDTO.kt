package ru.mephi.authentication.webclient.dto

import java.util.*

data class CreateUserDTO (
    val id : UUID,
    val username : String,
    val tag : String,
    val email : String,
    val showEmail: Boolean
)