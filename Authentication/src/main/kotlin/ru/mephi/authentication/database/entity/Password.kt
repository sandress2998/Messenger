package ru.mephi.authentication.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "passwords")
class Password(
    @Column
    var email: String,

    @Column(value = "hashed_password")
    var hashedPassword: String,

    @Column
    var userId: UUID,

    @Id
    val id: Long? = null,
)