package ru.mephi.messagehandler.models.exception

import ru.mephi.messagehandler.models.dto.rest.response.RequestResult

open class FailureResult(override val message: String): RequestResult, Throwable() {
    //override val result: RequestResult.Result = RequestResult.Result.FAILURE
}