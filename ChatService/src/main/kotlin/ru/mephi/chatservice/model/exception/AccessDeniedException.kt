package ru.mephi.chatservice.model.exception


class AccessDeniedException(causeOfDenied: Cause): FailureResult( "User doesn't have enough rights to execute the request: " + when (causeOfDenied) {
    Cause.NOT_MEMBER -> "user isn't a member of chat"
    Cause.NOT_ADMIN -> "user isn't an admin of chat"
}) {
    enum class Cause {
        NOT_MEMBER, NOT_ADMIN
    }
}