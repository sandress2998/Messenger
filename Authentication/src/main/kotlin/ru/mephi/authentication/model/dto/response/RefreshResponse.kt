package ru.mephi.authentication.model.dto.response

class RefreshResponse(
    val refresh: String,
    val jwt: String
): BaseResponse()