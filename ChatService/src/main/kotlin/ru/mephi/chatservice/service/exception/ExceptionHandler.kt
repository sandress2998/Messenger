package ru.mephi.chatservice.service.exception

import ru.mephi.chatservice.models.exception.NotFoundException
import ru.mephi.chatservice.models.responce.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.mephi.chatservice.models.exception.FailureResult

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex : NotFoundException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message,
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(FailureResult::class)
    fun handleFailureResult(ex : FailureResult) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message,
        )
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleFailureResult(ex : RuntimeException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "Unknown error",
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}