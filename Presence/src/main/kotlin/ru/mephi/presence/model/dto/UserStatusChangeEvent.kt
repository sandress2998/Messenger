package ru.mephi.presence.model.dto

data class UserStatusChangeEvent (
    val email: String,
    val status: String
)