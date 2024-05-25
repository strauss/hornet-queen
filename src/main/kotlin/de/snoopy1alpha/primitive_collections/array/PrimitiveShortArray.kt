package de.snoopy1alpha.primitive_collections.array

import de.snoopy1alpha.primitive_collections.SHORT_SIZE
import java.nio.ByteBuffer

class PrimitiveShortArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Short>(size, SHORT_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = false) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = (Int.MAX_VALUE - 2) / SHORT_SIZE
    }

    override operator fun get(index: Int): Short = internalGetShort(index)

    override operator fun set(index: Int, element: Short) {
        internalSetShort(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveShortArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveShortArray(newSize, native, newBuffer)
    }
}