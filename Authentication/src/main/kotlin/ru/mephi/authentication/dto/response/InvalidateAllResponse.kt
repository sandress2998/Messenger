package ru.mephi.authentication.dto.response

class InvalidateAllResponse(
    val allTokensInvalidated: String
): BaseResponse() {}