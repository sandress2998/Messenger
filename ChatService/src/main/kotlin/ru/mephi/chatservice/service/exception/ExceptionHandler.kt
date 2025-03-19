package ru.mephi.chatservice.service.exception

import ru.mephi.chatservice.models.exception.NotFoundException
import ru.mephi.chatservice.models.responce.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(ru.mephi.chatservice.models.exception.NotFoundException::class)
    fun handleNotFoundException(ex : ru.mephi.chatservice.models.exception.NotFoundException) : ResponseEntity<ru.mephi.chatservice.models.responce.ErrorResponse> {
        val errorResponse = ru.mephi.chatservice.models.responce.ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Internal server error",
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }
}