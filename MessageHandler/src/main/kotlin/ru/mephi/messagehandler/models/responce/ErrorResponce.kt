package ru.mephi.messagehandler.models.responce

data class ErrorResponse(
    val status: Int,
    val message: String
)