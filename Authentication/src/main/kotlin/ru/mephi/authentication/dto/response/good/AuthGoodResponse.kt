package ru.mephi.authentication.dto.response.good

import ru.mephi.authentication.dto.response.BaseResponse

class AuthGoodResponse(
    override val email: String,
    val refresh: String,
    val jwt: String
): BaseResponse()