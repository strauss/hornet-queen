package de.snoopy1alpha.primitive_collections.array

import de.snoopy1alpha.primitive_collections.LONG_SIZE
import java.nio.ByteBuffer

class PrimitiveLongArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Long>(size, LONG_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = false) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE / LONG_SIZE
    }

    override operator fun get(index: Int): Long = internalGetLong(index)

    override operator fun set(index: Int, element: Long) {
        internalSetLong(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveLongArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveLongArray(newSize, native, newBuffer)
    }
}