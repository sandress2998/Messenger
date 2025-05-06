package ru.mephi.authentication.model.dto.request

abstract class AuthBaseRequest {
    abstract val email: String
    abstract val password: String
}