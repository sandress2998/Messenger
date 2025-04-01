package ru.mephi.userservice.model.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionResolver {
    @ExceptionHandler(FailureResponse::class)
    fun handleFailureResponse(ex : FailureResponse) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message,
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }
}