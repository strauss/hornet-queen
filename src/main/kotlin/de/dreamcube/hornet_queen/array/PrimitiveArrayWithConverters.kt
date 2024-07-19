package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.ConfigurableConstants
import java.nio.ByteBuffer

class PrimitiveArrayWithConverters<T> private constructor(
    size: Int,
    elementSize: Int,
    native: Boolean,
    private val outConverter: (ByteArray) -> T,
    private val inConverter: (T) -> ByteArray,
    internalBuffer: ByteBuffer?
) : PrimitiveArray<T>(
    size,
    elementSize,
    native,
    (Int.MAX_VALUE - 8) / elementSize,
    internalBuffer
) {
    constructor(
        size: Int,
        elementSize: Int,
        outConverter: (ByteArray) -> T,
        inConverter: (T) -> ByteArray,
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
    ) : this(size, elementSize, native, outConverter, inConverter, null)

    override operator fun get(index: Int): T {
        val actualIndex = indexOf(index)
        val elementAsByteArray = ByteArray(elementSize)
        for (i in 0..<elementSize) {
            elementAsByteArray[i] = buffer[actualIndex + i]
        }
        return outConverter(elementAsByteArray)
    }

    override operator fun set(index: Int, element: T) {
        val actualIndex: Int = indexOf(index)
        val elementAsByteArray: ByteArray = inConverter(element)
        for (i in 0..<elementSize) {
            buffer[actualIndex + i] = elementAsByteArray[i]
        }
    }

    override fun getResizedCopy(difference: Int): PrimitiveArrayWithConverters<T> {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveArrayWithConverters(
            newSize,
            elementSize,
            native,
            outConverter,
            inConverter,
            newBuffer
        )
    }

}
