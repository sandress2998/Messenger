package ru.mephi.authentication.model.service

import io.jsonwebtoken.Claims

interface JwtService {
    fun generateToken(email: String): String

    fun validateToken(token: String): Claims
}