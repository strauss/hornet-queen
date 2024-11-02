/*
 * Hornet Queen
 * Copyright (c) 2024 Sascha Strauß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * Generic implementation of a primitive array. The underlying data structure is a [ByteBuffer] that either wraps a
 * [ByteArray] or allocates a [native] array. If a [native] allocation is requested, the array is not located in the
 * VMs heap space with all advantages (20% faster for all access operations) and disadvantages (unbound memory
 * allocation).
 */
abstract class PrimitiveArray<T>(
    val size: Int,
    protected val elementSize: Int,
    internal val native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    val maxSize: Int,
    internalBuffer: ByteBuffer? = null
) {
    val indices
        get() = 0..<size

    /**
     * The internal data structure.
     */
    internal val buffer: ByteBuffer = internalBuffer ?: when {
        native -> ByteBuffer.allocateDirect(size * elementSize).order(ByteOrder.nativeOrder())
        else -> ByteBuffer.allocate(size * elementSize).order(ByteOrder.nativeOrder())
    }

    /**
     * Calculates the actual index for the given index, according to the [elementSize].
     */
    protected fun indexOf(index: Int): Int = index * elementSize

    /**
     * Returns the element at the given logical [index].
     */
    abstract operator fun get(index: Int): T

    /**
     * Sets the given [element] at the given [index].
     */
    abstract operator fun set(index: Int, element: T)

    /**
     * Checks if the given [otherElement] is equal to the element at the given [index]. Allows
     * specialized classes to optimize this operation if required
     */
    open fun equalsAt(index: Int, otherElement: T) = this[index] == otherElement

    /**
     * Creates an iterator for this [PrimitiveArray].
     */
    operator fun iterator() = object : Iterator<T> {
        var currentIndex = 0

        override fun hasNext(): Boolean = currentIndex in 0..<size

        override fun next(): T {
            val next = get(currentIndex)
            currentIndex += 1
            return next
        }
    }

    /**
     * Creates a copy of this [PrimitiveArray] with different size. The given [difference] may be negative. This method
     * can be used in higher level data structures for resizing. If the array length is reduced, the elements at the end
     * are left out.
     */
    abstract fun getResizedCopy(difference: Int): PrimitiveArray<T>

    /**
     * Calculates the new size with the given [difference].
     * @throws IndexOutOfBoundsException if the new size exceeds the [maxSize] or the resulting new size is negative.
     */
    protected fun getNewSize(difference: Int): Int {
        val newSize: Int = size + difference
        if (newSize > maxSize) {
            throw IndexOutOfBoundsException("New size '$newSize' exceeds maximum size '$maxSize'.")
        }
        if (newSize < 0) {
            throw IndexOutOfBoundsException("Negative size '$newSize'!")
        }
        return newSize
    }

    /**
     * Internal function for creating a copy of this array. The copy is created on byte level.
     */
    protected fun getResizedCopyOfBuffer(newSize: Int): ByteBuffer {
        val oldBufferSize = buffer.capacity()
        val newBufferSize = newSize * elementSize
        // copy limit depends on expanding or reducing
        val copyLimit = min(oldBufferSize, newBufferSize)
        val newBuffer: ByteBuffer
        if (buffer.isDirect) {
            newBuffer = ByteBuffer.allocateDirect(newBufferSize).order(ByteOrder.nativeOrder())
            val arrayView = buffer.duplicate().rewind()
            while (arrayView.position() < copyLimit) {
                newBuffer.put(arrayView.get())
            }
        } else {
            val internalArray: ByteArray = buffer.array()
            val newArray = ByteArray(newBufferSize)
            System.arraycopy(internalArray, 0, newArray, 0, copyLimit)
            newBuffer = ByteBuffer.wrap(newArray).order(ByteOrder.nativeOrder())
        }
        return newBuffer
    }

    protected fun internalGetByte(index: Int): Byte {
        return buffer[index]
    }

    protected fun internalSetByte(index: Int, element: Byte) {
        buffer.put(index, element)
    }

    protected fun internalGetShort(index: Int): Short {
        return buffer.getShort(index shl SHORT_SHIFT)
    }

    protected fun internalGetChar(index: Int): Char {
        return buffer.getChar(index shl CHAR_SHIFT)
    }

    protected fun internalSetShort(index: Int, element: Short) {
        buffer.putShort(index shl SHORT_SHIFT, element)
    }

    protected fun internalSetChar(index: Int, element: Char) {
        buffer.putChar(index shl CHAR_SHIFT, element)
    }

    protected fun internalGetInt(index: Int): Int {
        return buffer.getInt(index shl INT_SHIFT)
    }

    protected fun internalSetInt(index: Int, element: Int) {
        buffer.putInt(index shl INT_SHIFT, element)
    }

    protected fun internalGetLong(index: Int): Long {
        return internalGetLongDirectly(index shl LONG_SHIFT)
    }

    protected fun internalSetLong(index: Int, element: Long) {
        internalSetLongDirectly(index shl LONG_SHIFT, element)
    }

    protected fun internalGetLongDirectly(index: Int): Long {
        return buffer.getLong(index)
    }

    protected fun internalSetLongDirectly(index: Int, element: Long) {
        buffer.putLong(index, element)
    }

    protected fun internalGetFloat(index: Int): Float {
        return buffer.getFloat(index shl FLOAT_SHIFT)
    }

    protected fun internalSetFloat(index: Int, element: Float) {
        buffer.putFloat(index shl FLOAT_SHIFT, element)
    }

    protected fun internalGetDouble(index: Int): Double {
        return buffer.getDouble(index shl DOUBLE_SHIFT)
    }

    protected fun internalSetDouble(index: Int, element: Double) {
        buffer.putDouble(index shl DOUBLE_SHIFT, element)
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("[")
        if (size > 0) {
            result.append(this[0])
            for (i in 1..<size) {
                result.append(",${this[i]}")
            }
        }
        result.append("]")
        return result.toString()
    }
}

internal operator fun ByteBuffer.set(index: Int, element: Byte) {
    this.put(index, element)
}