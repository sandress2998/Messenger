package ru.mephi.messagehandler.models.exception

open class FailureResult(override val message: String): Throwable() {
    //override val result: RequestResult.Result = RequestResult.Result.FAILURE
}