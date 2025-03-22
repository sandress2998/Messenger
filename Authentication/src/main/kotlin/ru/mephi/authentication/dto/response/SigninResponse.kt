package ru.mephi.authentication.dto.response

class SigninResponse(
    val refresh: String,
    val jwt: String
): BaseResponse()