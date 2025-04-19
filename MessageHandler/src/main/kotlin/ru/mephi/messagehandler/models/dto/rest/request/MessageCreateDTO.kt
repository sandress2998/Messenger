package ru.mephi.messagehandler.models.dto.rest.request

data class MessageCreateDTO(
    val text: String
)

/*
data class MessageCreateDTO(
    val chatId : UUID,
    val senderId: UUID,
    val text: String
)
*/
