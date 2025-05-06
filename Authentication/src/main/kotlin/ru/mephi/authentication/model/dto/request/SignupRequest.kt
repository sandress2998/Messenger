package ru.mephi.authentication.model.dto.request

data class SignupRequest (
    val username: String,
    val tag: String,
    override val email: String,
    val showEmail: Boolean,
    override val password: String
): AuthBaseRequest()