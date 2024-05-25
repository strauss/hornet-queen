package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.BYTE_SIZE
import java.nio.ByteBuffer

class PrimitiveByteArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Byte>(size, BYTE_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = false) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE - 8
    }

    override fun get(index: Int): Byte = internalGetByte(index)

    override fun set(index: Int, element: Byte) {
        internalSetByte(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveByteArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveByteArray(newSize, native, newBuffer)
    }
}