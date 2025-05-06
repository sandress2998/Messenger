package ru.mephi.websocket.model.dto.websocket.receive

/*
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
 */