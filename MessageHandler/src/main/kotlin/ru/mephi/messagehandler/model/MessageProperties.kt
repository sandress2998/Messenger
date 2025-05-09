package ru.mephi.messagehandler.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MessageProperties {
    @Value("\${my.messages.pagination-quantity}")
    var paginationMessagesQuantity: Int = 0
}