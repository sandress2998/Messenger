package ru.mephi.authentication.property

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecurityProperties {
    @Value("\${my.security.refresh-timeout-in-days}")
    var refreshTimeoutInDays: Long = 0

    @Value("\${my.security.jwt-timeout-in-minutes}")
    var jwtTimeoutInMinutes: Long = 0

    @Value("\${my.security.secret-string}")
    var secretString: String? = null
}