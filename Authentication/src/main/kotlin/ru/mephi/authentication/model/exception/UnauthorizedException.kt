package ru.mephi.authentication.model.exception

import org.springframework.http.HttpStatus

class UnauthorizedException (
    override val message: String = "Unauthorized",
): Throwable() {
    val status = HttpStatus.UNAUTHORIZED
}