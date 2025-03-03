package ru.mephi.authentication.dto.request

abstract class AuthBaseRequest {
    abstract val email: String
    abstract val password: String
}