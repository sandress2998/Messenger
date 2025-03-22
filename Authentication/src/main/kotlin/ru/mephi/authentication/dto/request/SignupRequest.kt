package ru.mephi.authentication.dto.request

class SignupRequest (
    override val email: String,
    override val password: String
): AuthBaseRequest()