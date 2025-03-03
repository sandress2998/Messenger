package ru.mephi.authentication.model.service

interface JwtService {
    fun generateToken(email: String): String
}