package ru.mephi.gateway.security

import io.jsonwebtoken.Claims

interface JwtService {
    fun validateToken(token: String): Claims
}