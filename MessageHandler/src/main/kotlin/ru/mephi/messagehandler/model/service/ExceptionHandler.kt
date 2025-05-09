package ru.mephi.messagehandler.model.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import ru.mephi.messagehandler.model.exception.NotFoundException
import ru.mephi.messagehandler.model.dto.rest.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.mephi.messagehandler.model.exception.AccessDeniedException

@RestControllerAdvice
class ExceptionHandler (
    private val registry: MeterRegistry
) {
    val internalError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")
        .tag("status", HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .register(registry)

    val notFoundError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")
        .tag("status", HttpStatus.NOT_FOUND.toString())
        .register(registry)

    val accessDeniedError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")
        .tag("status", HttpStatus.FORBIDDEN.toString())
        .register(registry)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex : NotFoundException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message!!,
        )
        notFoundError.increment()
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex : AccessDeniedException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message!!,
        )
        accessDeniedError.increment()
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeResult(ex : RuntimeException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "Unknown error",
        )
        internalError.increment()
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}