package ru.mephi.authentication.model.service

interface JwtService {
    fun generateToken(userId: String): String
}