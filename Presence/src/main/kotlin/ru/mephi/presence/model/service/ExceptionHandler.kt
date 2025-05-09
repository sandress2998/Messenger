package ru.mephi.presence.model.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.mephi.presence.model.dto.rest.ErrorResponse

@RestControllerAdvice
class ExceptionHandler (
    private val registry: MeterRegistry
) {
    val internalError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")
        .tag("status", HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .register(registry)

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