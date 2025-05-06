package ru.mephi.authentication.model.dto.response

class SigninResponse(
    val refresh: String,
    val jwt: String
): BaseResponse()