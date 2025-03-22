package ru.mephi.authentication.model.service.impl

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import ru.mephi.authentication.model.exception.UnauthorizedException
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.property.SecurityProperties
import java.security.Key
import java.util.*

@Service
class JwtServiceImpl(
    private val securityProperties: SecurityProperties
): JwtService {
    private val secretKey: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProperties.secretString))

    override fun generateToken(email: String): String {
        return Jwts.builder()
            .subject(email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + securityProperties.jwtTimeoutInMinutes * 60 * 1000))
            .signWith(secretKey)
            .compact()
    }

    override fun validateToken(token: String): Claims {
        return try {
            Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (ex: ExpiredJwtException) {
            println("Token is expired: ${ex.message}")
            throw UnauthorizedException("Token is expired: ${ex.message}")
        } catch (ex: JwtException) {
            println("JWT parsing error: ${ex.message}")
            throw UnauthorizedException("Invalid token: ${ex.message}")
        } catch (ex: Exception) {
            println("Unexpected error during token validation")
            throw UnauthorizedException("Unexpected error during token validation")
        }
    }
}