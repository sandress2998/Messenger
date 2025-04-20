package ru.mephi.authentication.dto.request

data class SigninRequest (
    override val email: String,
    override val password: String
): AuthBaseRequest()