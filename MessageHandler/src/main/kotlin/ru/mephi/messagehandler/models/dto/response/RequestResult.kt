package ru.mephi.messagehandler.models.dto.response

interface RequestResult {
    val result: Result

    enum class Result {
        SUCCESS, FAILURE
    }
}
