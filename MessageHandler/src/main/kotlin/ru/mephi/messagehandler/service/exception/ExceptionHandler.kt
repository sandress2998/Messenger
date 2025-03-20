package ru.mephi.messagehandler.service.exception

import ru.mephi.messagehandler.models.exception.NotFoundException
import ru.mephi.messagehandler.models.responce.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex : NotFoundException) : ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Not found",
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }
}