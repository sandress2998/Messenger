package ru.mephi.authentication.dto.response

class SignupResponse(
    val refresh: String,
    val jwt: String
): BaseResponse()