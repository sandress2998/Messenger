package ru.mephi.authentication.model.service.impl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Service
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.JwtService.Companion.CLASS_NAME
import ru.mephi.authentication.property.SecurityProperties
import java.security.Key
import java.util.*

@Service
class JwtServiceImpl(
    private val securityProperties: SecurityProperties
): JwtService {
    private val secretKey: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProperties.secretString))

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations",
        extraTags = ["operation", "$CLASS_NAME.generateToken"]  // пары ключ-значение
    )
    override fun generateToken(userId: String): String {
        return Jwts.builder()
            .subject(userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + securityProperties.jwtTimeoutInMinutes * 60 * 1000))
            .signWith(secretKey)
            .compact()
    }
}