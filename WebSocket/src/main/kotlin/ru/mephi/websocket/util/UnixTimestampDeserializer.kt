package ru.mephi.websocket.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigDecimal
import java.time.Instant

class UnixTimestampDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
        return if (p.currentToken.isNumeric) {
            val decimalValue = p.decimalValue // Получаем как BigDecimal
            val seconds = decimalValue.toLong()
            val nanos = decimalValue.remainder(BigDecimal.ONE)
                .movePointRight(9)
                .toLong()
            Instant.ofEpochSecond(seconds, nanos)
        } else {
            Instant.parse(p.text) // Резервный вариант для ISO-формата
        }
    }
}

