package ru.mephi.userservice.model.dto

data class UpdateUserRequest (
    val username: String,
    val tag: String,
    val email: String,
    val showEmail: Boolean
)