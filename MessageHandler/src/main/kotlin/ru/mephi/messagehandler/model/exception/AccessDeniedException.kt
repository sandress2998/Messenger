package ru.mephi.messagehandler.model.exception

class AccessDeniedException(causeOfDenied: Cause): RuntimeException("User doesn't have enough rights to execute the request: " + when (causeOfDenied) {
    Cause.NOT_MEMBER -> "User isn't a member of chat"
    Cause.NOT_ADMIN -> "User isn't an admin of chat"
    Cause.NOT_SENDER -> "User isn't a sender of message"
}) {
    enum class Cause {
        NOT_MEMBER, NOT_ADMIN, NOT_SENDER
    }
}