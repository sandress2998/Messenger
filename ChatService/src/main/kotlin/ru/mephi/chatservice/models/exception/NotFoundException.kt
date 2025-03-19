package ru.mephi.chatservice.models.exception

class NotFoundException(override val message: String?) : RuntimeException(message)