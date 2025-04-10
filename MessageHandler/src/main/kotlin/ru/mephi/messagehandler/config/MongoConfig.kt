package ru.mephi.messagehandler.config

import org.bson.types.Binary
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import ru.mephi.messagehandler.util.UUIDUtil
import java.util.*

@Configuration
class MongoConfig {
    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(listOf(
            UuidToBinaryConverter(),
            BinaryToUuidConverter()
        ))
    }

    @WritingConverter
    class UuidToBinaryConverter : Converter<UUID, Binary> {
        override fun convert(source: UUID): Binary = UUIDUtil.toBinary(source)
    }

    @ReadingConverter
    class BinaryToUuidConverter : Converter<Binary, UUID> {
        override fun convert(source: Binary): UUID = UUIDUtil.fromBinary(source)
    }
}