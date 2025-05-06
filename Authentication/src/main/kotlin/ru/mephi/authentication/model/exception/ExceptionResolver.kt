package ru.mephi.authentication.model.exception

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.mephi.authentication.model.dto.response.ErrorResponse


@RestControllerAdvice
class ExceptionResolver (
    private val registry: MeterRegistry
) {
    val internalError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")  // Описание метрики // Второй тег
        .tag("status", HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .register(registry)                     // Регистрация в реестре

    val unauthorizedError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")  // Описание метрики // Второй тег
        .tag("status", HttpStatus.UNAUTHORIZED.toString())
        .register(registry)                     // Регистрация в реестре


    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        // Логирование ошибки
        println("Handling UnauthorizedException: ${ex.message}")

        // Создание объекта ошибки
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(), // Тип ошибки (например, "Unauthorized")
            message = ex.message// Сообщение об ошибке
        )
        unauthorizedError.increment()
        // Возврат ответа с кодом 401 и объектом ошибки
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        // Логирование ошибки
        println("Handling RuntimeException: ${ex.message}")

        // Создание объекта ошибки
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(), // Тип ошибки (например, "Unauthorized")
            message = ex.message ?: "Unauthorized" // Сообщение об ошибке
        )
        internalError.increment()
        // Возврат ответа с кодом 500 и объектом ошибки
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse)
    }
}
