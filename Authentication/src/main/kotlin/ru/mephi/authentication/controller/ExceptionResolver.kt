package ru.mephi.authentication.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange
import ru.mephi.authentication.dto.response.bad.ErrorResponse
import ru.mephi.authentication.model.exception.UnauthorizedException


@ControllerAdvice
class ExceptionResolver {
    @ExceptionHandler(UnauthorizedException::class)
    fun handle(ex: UnauthorizedException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        // Логирование ошибки
        println("Handling UnauthorizedException: ${ex.message}")

        // Создание объекта ошибки
        val errorResponse = ErrorResponse(
            error = HttpStatus.UNAUTHORIZED.reasonPhrase, // Тип ошибки (например, "Unauthorized")
            message = ex.message ?: "Unauthorized" // Сообщение об ошибке
        )

        // Возврат ответа с кодом 401 и объектом ошибки
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse)
    }
}
