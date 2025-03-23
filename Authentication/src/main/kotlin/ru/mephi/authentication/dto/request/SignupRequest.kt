package ru.mephi.authentication.dto.request

class SignupRequest (
    val username: String,
    override val email: String,
    override val password: String
): AuthBaseRequest()