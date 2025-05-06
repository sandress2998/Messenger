package ru.mephi.chatservice.model.dto.rest

data class UserInfo (
    val username: String,
    val tag: String,
    val email: String?
): SuccessResult()
