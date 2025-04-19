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
        return MongoCustomConversions (listOf(
            UuidToBinaryConverter(),
            BinaryToUuidConverter(),
            UuidListToBinaryArrayListConverter(),
            BinaryListToUuidArrayListConverter(),
            AnyArrayListToUuidListConverter()
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

    @WritingConverter
    class UuidListToBinaryArrayListConverter : Converter<ArrayList<UUID>, ArrayList<Binary>> {
        override fun convert(source: ArrayList<UUID>): ArrayList<Binary> = UUIDUtil.toBinaryArrayList(source)
    }

    @ReadingConverter
    class BinaryListToUuidArrayListConverter : Converter<ArrayList<Binary>, ArrayList<UUID>> {
        override fun convert(source: ArrayList<Binary>): ArrayList<UUID> = UUIDUtil.fromBinaryArrayList(source)
    }

    @ReadingConverter
    class AnyArrayListToUuidListConverter : Converter<ArrayList<*>, ArrayList<UUID>> {
        override fun convert(source: ArrayList<*>): ArrayList<UUID> {
            return source.map {
                when (it) {
                    is Binary -> UUIDUtil.fromBinary(it)
                    is UUID -> it
                    else -> throw IllegalArgumentException("Unsupported type: ${it?.javaClass}")
                }
            }.filterIsInstance<UUID>()
                .toCollection(ArrayList())
        }
    }
}