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

package de.dreamcube.hornet_queen.list

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import java.util.*

/**
 * Primitive list with a primitive array as underlying data structure. [get] and [set] are of constant complexity. All
 * modifying operations, that are index based, require a linear shift operation. It behaves similarly to the
 * [java.util.ArrayList], except it uses a more space efficient variant of the internal data structure
 * ([PrimitiveArray]).
 */
abstract class PrimitiveArrayList<T> protected constructor(
    arraySupplier: (Int) -> PrimitiveArray<T>,
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE
) : PrimitiveArrayBasedList<T>(), RandomAccess {

    private var array: PrimitiveArray<T> = arraySupplier(initialSize)
    private var nextFreeIndex = 0

    override val size: Int
        get() = nextFreeIndex

    private fun grow() {
        val oldCapacity: Int = array.size
        val newCapacity: Int = array.calculateSizeForGrow()
        array = array.getResizedCopy(newCapacity - oldCapacity)
    }

    override fun trimToSize() {
        array = array.getResizedCopy(size - array.size)
    }

    override fun getInternalArraySize(): Int = array.size

    override fun getInternalArrayMaxSize(): Int = array.maxSize

    override fun add(element: T): Boolean {
        if (size == array.size) {
            grow()
        }
        array[nextFreeIndex] = element
        nextFreeIndex += 1
        return true
    }

    private fun shift(index: Int, by: Int) {
        if (index !in 0..size) {
            throw IndexOutOfBoundsException(index)
        }
        if (index + by !in 0 + by..size + by) {
            throw IndexOutOfBoundsException(index + by)
        }
        if (by == 0) {
            return
        }
        if (size + by > array.size) {
            grow()
        }
        if (by > 0) {
            // this operation frees up space (right shift)
            for (i in size - 1 downTo index) {
                array[i + by] = array[i]
            }
            // theoretically we could delete the positions in between, but this is not required
        } else {
            // this operation removes elements (left shift)
            assert(by < 0)
            for (i in index - by..<size) { // caution, "by" is negative and we are actually adding here!!!
                array[i + by] = array[i] // caution, "by" is negative and we are actually subtracting here!!!
            }
        }
        // we could update the currentPosition and the size, but we leave that to the add/remove operations
    }

    override fun add(index: Int, element: T) {
        shift(index, 1)
        array[index] = element
        nextFreeIndex += 1
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val numberOfElements: Int = elements.size
        shift(index, numberOfElements)
        var i = index
        for (currentElement: T in elements) {
            array[i] = currentElement
            i += 1
        }
        nextFreeIndex += numberOfElements
        return true
    }

    override fun clear() {
        // technically we could clear all positions, but this is a tiny little bit faster
        nextFreeIndex = 0
    }

    override fun get(index: Int): T {
        if (index !in 0..<size) {
            throw IndexOutOfBoundsException(index)
        }
        return array[index]
    }

    override fun isEmpty(): Boolean {
        return nextFreeIndex == 0
    }

    override fun iterator(): MutableIterator<T> = PrimitiveArrayListIterator()

    override fun listIterator(): MutableListIterator<T> = PrimitiveArrayListIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = PrimitiveArrayListIterator(initIndex = index)

    override fun rangedListIterator(startIndex: Int, endIndex: Int): MutableListIterator<T> =
        PrimitiveArrayListIterator(startIndex, endIndex)

    override fun removeAt(index: Int): T {
        val element: T = this[index]
        shift(index, -1)
        nextFreeIndex -= 1
        return element
    }

    override operator fun set(index: Int, element: T): T {
        if (index !in 0..<size) {
            throw IndexOutOfBoundsException(index)
        }
        val oldElement = array[index]
        array[index] = element
        return oldElement
    }

    override fun lastIndexOf(element: T): Int {
        for (i in size - 1 downTo 0) {
            if (array[i] == element) {
                return i
            }
        }
        return -1
    }

    override fun indexOf(element: T): Int {
        for (i in 0..<size) {
            if (array[i] == element) {
                return i
            }
        }
        return -1
    }

    /**
     * [MutableListIterator] for [PrimitiveArrayList] for iterating from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     */
    inner class PrimitiveArrayListIterator(startIndex: Int = 0, endIndex: Int = size, initIndex: Int = startIndex) :
        AbstractArrayBasedListIterator<T>(startIndex, endIndex, initIndex) {

        init {
            if (isNotEmpty()) {
                checkBounds(startIndex, endIndex, size, nextIndex)
            }
        }

        override fun add(element: T) {
            this@PrimitiveArrayList.add(nextIndex, element)
            nextIndex += 1
            endIndex += 1
            callState = IteratorCallState.ADD
        }

        override fun next(): T {
            val result: T = array[nextIndex]
            nextIndex += 1
            callState = IteratorCallState.NEXT
            return result
        }

        override fun previous(): T {
            nextIndex -= 1
            callState = IteratorCallState.PREV
            return array[nextIndex]
        }

        override fun remove() {
            when (callState) {
                IteratorCallState.NEXT -> {
                    nextIndex -= 1
                    endIndex -= 1
                    removeAt(nextIndex)
                }

                IteratorCallState.PREV -> {
                    // when previous was called, the next index is placed at the element previously returned
                    endIndex -= 1
                    removeAt(nextIndex)
                }

                else -> error("$callState")
            }
            callState = IteratorCallState.REM
        }

        override fun set(element: T) {
            when (callState) {
                IteratorCallState.NEXT -> {
                    array[nextIndex - 1] = element
                }

                IteratorCallState.PREV -> {
                    array[nextIndex] = element
                }

                else -> error("$callState")
            }
            callState = IteratorCallState.SET
        }

    }
}

class PrimitiveByteArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Byte>({ size: Int -> PrimitiveByteArray(size, native) }, initialSize)

class PrimitiveShortArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Short>({ size: Int -> PrimitiveShortArray(size, native) }, initialSize)

class PrimitiveCharArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Char>({ size: Int -> PrimitiveCharArray(size, native) }, initialSize)

class PrimitiveIntArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Int>({ size: Int -> PrimitiveIntArray(size, native) }, initialSize)

class PrimitiveLongArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Long>({ size: Int -> PrimitiveLongArray(size, native) }, initialSize)

class PrimitiveFloatArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Float>({ size: Int -> PrimitiveFloatArray(size, native) }, initialSize)

class PrimitiveDoubleArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<Double>({ size: Int -> PrimitiveDoubleArray(size, native) }, initialSize)

class UUIDArrayList
@JvmOverloads constructor(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveArrayList<UUID>({ size: Int -> UUIDArray(size, native) }, initialSize)

internal class InternalPrimitiveTypeList<T>(
    arraySupplier: (Int) -> PrimitiveArray<T>,
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE
) : PrimitiveArrayList<T>(arraySupplier, initialSize)
