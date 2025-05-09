package ru.mephi.userservice.model.dto

data class ErrorResponse (
    val status: Int,
    val message: String
)