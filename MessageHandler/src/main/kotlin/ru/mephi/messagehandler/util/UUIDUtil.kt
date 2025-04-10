package ru.mephi.messagehandler.util

import org.bson.BsonBinarySubType
import org.bson.types.Binary
import java.nio.ByteBuffer
import java.util.*

object UUIDUtil {
    fun toBinary(uuid: UUID): Binary {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return Binary(BsonBinarySubType.UUID_STANDARD, bb.array())
    }

    fun fromBinary(binary: Binary): UUID {
        val bb = ByteBuffer.wrap(binary.data)
        val mostSigBits = bb.long
        val leastSigBits = bb.long
        return UUID(mostSigBits, leastSigBits)
    }
}