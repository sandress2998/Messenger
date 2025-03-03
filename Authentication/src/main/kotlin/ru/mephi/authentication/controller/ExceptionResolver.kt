package ru.mephi.authentication.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange
import ru.mephi.authentication.model.exception.UnauthorizedException


@ControllerAdvice
class ExceptionResolver {
    @ExceptionHandler(UnauthorizedException::class)
    fun handle(ex: UnauthorizedException, exchange: ServerWebExchange): ResponseEntity<Pair<HttpStatus, String>> {
        // Логирование ошибки
        println("Handling UnauthorizedException: ${ex.message}")

        // Возврат ответа с кодом 401 и сообщением об ошибке
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Pair(HttpStatus.UNAUTHORIZED, ex.message ?: "Unauthorized"))
    }
}
