package ru.mephi.chatservice.model.dto

data class ErrorResponse (
    val status: Int,
    val message: String
)