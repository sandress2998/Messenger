package ru.mephi.authentication.dto.request

data class RefreshRequest (
    val email: String,
    val refreshToken: String
)