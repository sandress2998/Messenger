package ru.mephi.authentication.dto.response

class SignoutResponse(
    override val email: String,
    val message: String
): BaseResponse()