package ru.mephi.chatservice.model.exception

class BadRequestException(causeOfDenied: Cause) : RuntimeException( "Unsuccessful: " + when (causeOfDenied) {
    Cause.ADMIN_ABSENCE -> "chat must have at least one admin"
    Cause.ALREADY_IN_CHAT -> "user is already in chat"
}) {
    enum class Cause {
        ADMIN_ABSENCE, ALREADY_IN_CHAT
    }
}