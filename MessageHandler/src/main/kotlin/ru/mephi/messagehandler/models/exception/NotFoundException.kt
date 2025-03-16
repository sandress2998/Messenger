package ru.mephi.messagehandler.models.exception

class NotFoundException(override val message: String?) : RuntimeException(message)