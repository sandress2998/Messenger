package ru.mephi.presence.model.dto

data class UserStatusChangeBroadcast (
    val email: String,
    val status: String,
    val receivers: List<String>
)