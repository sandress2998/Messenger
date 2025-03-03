package ru.mephi.authentication.dto.request

data class JwtTokenRequest (
    val email: String,
    val refreshToken: String
)