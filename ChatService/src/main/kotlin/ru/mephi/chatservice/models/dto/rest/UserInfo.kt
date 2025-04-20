package ru.mephi.chatservice.models.dto.rest

data class UserInfo (
    val username: String,
    val tag: String,
    val email: String?
): SuccessResult()
