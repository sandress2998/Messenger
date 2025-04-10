package ru.mephi.messagehandler.models.dto.response

open class SuccessResult: RequestResult {
    override val result: RequestResult.Result = RequestResult.Result.SUCCESS
}