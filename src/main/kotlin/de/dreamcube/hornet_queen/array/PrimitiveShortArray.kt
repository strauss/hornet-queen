package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.SHORT_SIZE
import java.nio.ByteBuffer

class PrimitiveShortArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Short>(size, SHORT_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / SHORT_SIZE
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