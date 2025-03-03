package ru.mephi.authentication.dto.response.good

import ru.mephi.authentication.dto.response.BaseResponse

class JwtGoodResponse(
    override val email: String,
    val jwt: String
): BaseResponse()