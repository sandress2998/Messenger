package ru.mephi.authentication.model.service

interface JwtService {
    companion object {
        const val CLASS_NAME = "JwtService"
    }

    fun generateToken(userId: String): String
}