package de.snoopy1alpha.primitive_collections.array

import de.snoopy1alpha.primitive_collections.LONG_SIZE
import de.snoopy1alpha.primitive_collections.UUID_SHIFT
import de.snoopy1alpha.primitive_collections.UUID_SIZE
import java.nio.ByteBuffer
import java.util.*

class UUIDArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<UUID>(size, UUID_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = false) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE / UUID_SIZE
    }

    override operator fun get(index: Int): UUID {
        val lowerIndex = index shl UUID_SHIFT
        val upperIndex = lowerIndex + LONG_SIZE
        val lower: Long = internalGetLongDirectly(lowerIndex)
        val upper: Long = internalGetLongDirectly(upperIndex)
        return UUID(upper, lower)
    }

    override operator fun set(index: Int, element: UUID) {
        val lowerIndex = index shl UUID_SHIFT
        val upperIndex = lowerIndex + LONG_SIZE
        internalSetLongDirectly(lowerIndex, element.leastSignificantBits)
        internalSetLongDirectly(upperIndex, element.mostSignificantBits)
    }

    override fun equalsAt(index: Int, otherElement: UUID): Boolean {
        val lowerIndex = index shl UUID_SHIFT
        val lower = internalGetLongDirectly(lowerIndex)
        if (lower == otherElement.leastSignificantBits) {
            val upperIndex = lowerIndex + LONG_SIZE
            val upper = internalGetLongDirectly(upperIndex)
            return upper == otherElement.mostSignificantBits
        }
        return false
    }

    override fun getResizedCopy(difference: Int): UUIDArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return UUIDArray(newSize, native, newBuffer)
    }

}