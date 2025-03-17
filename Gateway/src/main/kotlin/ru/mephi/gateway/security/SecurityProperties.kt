package ru.mephi.gateway.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

class SecurityProperties {
    @Value("\${my.security.secret-string}")
    var secretString: String? = null
}