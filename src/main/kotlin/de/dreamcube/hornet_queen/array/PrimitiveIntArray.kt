package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.DEFAULT_NATIVE
import de.dreamcube.hornet_queen.INT_SIZE
import java.nio.ByteBuffer

class PrimitiveIntArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Int>(size, INT_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE / INT_SIZE
    }

    override operator fun get(index: Int): Int = internalGetInt(index)

    override operator fun set(index: Int, element: Int) {
        internalSetInt(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveIntArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveIntArray(newSize, native, newBuffer)
    }
}