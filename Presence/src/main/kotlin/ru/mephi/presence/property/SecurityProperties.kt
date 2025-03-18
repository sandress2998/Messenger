package ru.mephi.presence.property

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecurityProperties {
    @Value("\${my.security.jwt-timeout-in-minutes}")
    var jwtTimeoutInMinutes: Long = 0

}