package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.CHAR_SIZE
import de.dreamcube.hornet_queen.ConfigurableConstants
import java.nio.ByteBuffer

class PrimitiveCharArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Char>(size, CHAR_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / CHAR_SIZE
    }

    override operator fun get(index: Int): Char = internalGetChar(index)

    override operator fun set(index: Int, element: Char) {
        internalSetChar(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveArray<Char> {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveCharArray(newSize, native, newBuffer)
    }
}