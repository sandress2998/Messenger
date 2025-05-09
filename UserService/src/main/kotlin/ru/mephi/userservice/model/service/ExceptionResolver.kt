package ru.mephi.userservice.model.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import ru.mephi.userservice.model.dto.ErrorResponse
import ru.mephi.userservice.model.exception.BadRequestException
import ru.mephi.userservice.model.exception.NotFoundException

@ControllerAdvice
class ExceptionResolver (
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

    val badRequestError = Counter.builder("api.error")
        .description("Total quantity of errors in requests.")
        .tag("status", HttpStatus.BAD_REQUEST.toString())
        .register(registry)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex : NotFoundException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message,
        )
        notFoundError.increment()
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex : BadRequestException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message
        )
        badRequestError.increment()
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleInternalError(ex : RuntimeException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "Unknown error",
        )
        internalError.increment()
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}