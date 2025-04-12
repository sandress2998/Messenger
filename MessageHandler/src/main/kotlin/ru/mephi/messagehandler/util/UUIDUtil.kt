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
        println("🔵 Converting UUID $uuid to Binary: $bb")
        return Binary(BsonBinarySubType.UUID_STANDARD, bb.array())
    }

    fun fromBinary(binary: Binary): UUID {
        val bb = ByteBuffer.wrap(binary.data)
        val mostSigBits = bb.long
        val leastSigBits = bb.long
        println("🔵 Converting Binary $bb to UUID ${UUID(mostSigBits, leastSigBits)}")
        return UUID(mostSigBits, leastSigBits)
    }

    fun toBinaryArrayList(source: ArrayList<UUID>): ArrayList<Binary> {
        return source.mapTo(ArrayList()) { uuid ->
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            Binary(BsonBinarySubType.UUID_STANDARD, bb.array())
        }
    }

    fun fromBinaryArrayList(binaryList: ArrayList<Binary>): ArrayList<UUID> {
        return binaryList.mapTo(ArrayList()) { binary ->
            val bb = ByteBuffer.wrap(binary.data)
            UUID(bb.long, bb.long)
        }
    }
}