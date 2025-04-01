package ru.mephi.userservice.model.exception

data class ErrorResponse (
    val status: Int,
    val message: String
)