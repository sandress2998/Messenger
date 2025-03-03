package ru.mephi.authentication.dto.request


data class SignoutRequest(
    val email: String,
    val refreshToken: String
)