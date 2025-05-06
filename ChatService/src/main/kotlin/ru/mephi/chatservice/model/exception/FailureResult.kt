package ru.mephi.chatservice.model.exception

import ru.mephi.chatservice.model.dto.rest.RequestResult

open class FailureResult (
    override val message: String
): RequestResult, RuntimeException() {
    //override val result: RequestResult.Result = RequestResult.Result.FAILURE
}