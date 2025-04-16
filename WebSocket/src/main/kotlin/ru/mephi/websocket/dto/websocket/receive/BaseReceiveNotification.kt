package ru.mephi.websocket.dto.websocket.receive

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "category" // Поле, по которому определяется тип
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ChatActivityChangeIngoingNotification::class, name = "activity_status")
    // Добавьте другие классы-наследники здесь
)
abstract class BaseReceiveNotification {
    abstract val category: NotificationReceiveCategory
}