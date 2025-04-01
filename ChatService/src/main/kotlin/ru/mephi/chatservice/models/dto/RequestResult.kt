package ru.mephi.chatservice.models.dto

interface RequestResult {
    val result: Result

    enum class Result {
        SUCCESS, FAILURE
    }
}
