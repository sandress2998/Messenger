package ru.mephi.userservice.model.dto

import ru.mephi.userservice.model.ActivityStatus

data class UserInfo (
    val username : String,
    val tag: String,
    val activity: ActivityStatus?,
    val email : String? = null,
    val showEmail: Boolean? = null
)