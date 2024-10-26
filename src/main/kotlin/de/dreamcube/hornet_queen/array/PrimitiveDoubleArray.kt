package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.DOUBLE_SIZE
import java.nio.ByteBuffer

class PrimitiveDoubleArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Double>(size, DOUBLE_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / DOUBLE_SIZE
    }

    override fun get(index: Int): Double = internalGetDouble(index)

    override fun set(index: Int, element: Double) {
        internalSetDouble(index, element)
    }

    override fun getResizedCopy(difference: Int): PrimitiveDoubleArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveDoubleArray(newSize, native, newBuffer)
    }
}