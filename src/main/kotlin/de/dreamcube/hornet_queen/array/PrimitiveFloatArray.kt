package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.FLOAT_SIZE
import java.nio.ByteBuffer

class PrimitiveFloatArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Float>(size, FLOAT_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / FLOAT_SIZE
    }

    override fun get(index: Int): Float = internalGetFloat(index)

    override fun set(index: Int, element: Float) {
        internalSetFloat(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveFloatArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveFloatArray(newSize, native, newBuffer)
    }
}