package ru.mephi.presence.model.dto.rest

data class ErrorResponse(
    val status: Int,
    val message: String
)