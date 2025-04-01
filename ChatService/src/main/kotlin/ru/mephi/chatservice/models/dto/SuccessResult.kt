package ru.mephi.chatservice.models.dto

open class SuccessResult: RequestResult {
    override val result: RequestResult.Result = RequestResult.Result.SUCCESS
}