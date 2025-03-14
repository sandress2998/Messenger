package ru.mephi.gateway.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import java.util.*

class JwtServiceImpl(
    private val securityProperties: SecurityProperties
): JwtService {
    private val secretKey: String = securityProperties.secretString!!

    override fun validateToken(token: String): Claims {
        return try {
            Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (ex: ExpiredJwtException) {
            println("Token is expired: ${ex.message}")
            throw RuntimeException("Token is expired: ${ex.message}")
        } catch (ex: JwtException) {
            println("JWT parsing error: ${ex.message}")
            throw RuntimeException("Invalid token: ${ex.message}")
        } catch (ex: Exception) {
            println("Unexpected error during token validation")
            throw RuntimeException("Unexpected error during token validation")
        }
    }
}