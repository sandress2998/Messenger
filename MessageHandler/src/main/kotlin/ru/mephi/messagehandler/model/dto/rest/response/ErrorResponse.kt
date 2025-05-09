package ru.mephi.messagehandler.model.dto.rest.response

data class ErrorResponse(
    val status: Int,
    val message: String
)