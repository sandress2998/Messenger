package ru.mephi.authentication.dto.request

class SigninRequest (
    override val email: String,
    override val password: String
): AuthBaseRequest()