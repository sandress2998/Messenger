package ru.mephi.authentication.model.dto.response

class InvalidateAllResponse(
    val allTokensInvalidated: String
): BaseResponse() {}