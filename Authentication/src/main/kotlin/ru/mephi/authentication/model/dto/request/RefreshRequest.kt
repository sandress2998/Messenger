package ru.mephi.authentication.model.dto.request

data class RefreshRequest (
    val email: String,
    val refreshToken: String
)