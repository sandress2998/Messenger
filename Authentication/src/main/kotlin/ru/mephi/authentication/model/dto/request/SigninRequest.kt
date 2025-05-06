package ru.mephi.authentication.model.dto.request

data class SigninRequest (
    override val email: String,
    override val password: String
): AuthBaseRequest()