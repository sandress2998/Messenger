package ru.mephi.userservice.model.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

data class GetUserDTO (
    val username : String,
    val email : String
)