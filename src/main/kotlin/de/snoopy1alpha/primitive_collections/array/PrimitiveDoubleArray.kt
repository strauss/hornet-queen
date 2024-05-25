package de.snoopy1alpha.primitive_collections.array

import de.snoopy1alpha.primitive_collections.DOUBLE_SIZE
import java.nio.ByteBuffer

class PrimitiveDoubleArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Double>(size, DOUBLE_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = false) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE / DOUBLE_SIZE
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