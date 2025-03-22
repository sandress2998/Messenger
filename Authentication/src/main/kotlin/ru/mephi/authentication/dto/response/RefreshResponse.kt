package ru.mephi.authentication.dto.response

class RefreshResponse(
    val refresh: String,
    val jwt: String
): BaseResponse()