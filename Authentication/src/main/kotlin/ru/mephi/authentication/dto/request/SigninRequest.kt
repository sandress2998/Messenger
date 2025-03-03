package ru.mephi.authentication.dto.request

import ru.mephi.authentication.dto.request.AuthBaseRequest

class SigninRequest (
    override val email: String,
    override val password: String
): AuthBaseRequest() {}