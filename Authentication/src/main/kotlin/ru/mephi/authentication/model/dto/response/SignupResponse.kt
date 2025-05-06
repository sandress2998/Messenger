package ru.mephi.authentication.model.dto.response

class SignupResponse(
    val refresh: String,
    val jwt: String
): BaseResponse()