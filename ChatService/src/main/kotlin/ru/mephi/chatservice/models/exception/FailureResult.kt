package ru.mephi.chatservice.models.exception

import ru.mephi.chatservice.models.dto.RequestResult

open class FailureResult (
    override val message: String
): RequestResult, RuntimeException() {
    override val result: RequestResult.Result = RequestResult.Result.FAILURE
}