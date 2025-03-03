package ru.mephi.authentication.dto.response.bad

import ru.mephi.authentication.dto.response.BaseResponse

class BadResponse(
    override val email: String,
    val message: String
): BaseResponse()