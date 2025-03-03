package ru.mephi.authentication.model.service.impl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.UserService
import ru.mephi.authentication.property.SecurityProperties
import java.security.Key
import java.util.*

@Service
class JwtServiceImpl(
    private val securityProperties: SecurityProperties
): JwtService {
    private val secretKey: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProperties.secretString))
    private val log: Logger = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    override fun generateToken(email: String): String {
        return Jwts.builder()
            .subject(email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + securityProperties.jwtTimeoutInMinutes * 60 * 1000))
            .signWith(secretKey)
            .compact()
    }
}