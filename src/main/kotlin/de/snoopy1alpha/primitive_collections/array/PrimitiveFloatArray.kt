package de.snoopy1alpha.primitive_collections.array

import de.snoopy1alpha.primitive_collections.FLOAT_SIZE
import java.nio.ByteBuffer

class PrimitiveFloatArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Float>(size, FLOAT_SIZE, native, MAX_SIZE, internalBuffer) {
    constructor(size: Int, native: Boolean = false) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE / FLOAT_SIZE
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