package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.LONG_SIZE
import java.nio.ByteBuffer

class PrimitiveLongArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Long>(size, LONG_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / LONG_SIZE
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