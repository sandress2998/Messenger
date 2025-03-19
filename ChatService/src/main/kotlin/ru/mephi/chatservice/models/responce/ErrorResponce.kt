package ru.mephi.chatservice.models.responce

data class ErrorResponse(
    val status: Int,
    val message: String
)