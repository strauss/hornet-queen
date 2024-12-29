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

/**
 * Interface defining lists based on primitive arrays implemented in this library.
 */
abstract class PrimitiveArrayBasedList<T> : MutableList<T> {
    override fun contains(element: T): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }

    /**
     * Shrinks the internal array to the size of the list. This operation should only be called if no more elements are
     * added to the list.
     */
    abstract fun trimToSize()

    internal abstract fun getInternalArraySize(): Int

    internal abstract fun getInternalArrayMaxSize(): Int

    override fun addAll(elements: Collection<T>): Boolean {
        for (currentElement: T in elements) {
            add(currentElement)
        }
        return true
    }

    /**
     * Returns an iterator with both lower [startIndex] and upper bound [endIndex].
     * @throws IndexOutOfBoundsException if any given index is out of bounds
     * @throws IllegalStateException if the [startIndex] is bigger than [endIndex]
     */
    abstract fun rangedListIterator(startIndex: Int, endIndex: Int): MutableListIterator<T>

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return MutableSubListView(this, fromIndex, toIndex)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var result = false
        val iterator: MutableIterator<T> = iterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (!elements.contains(current)) {
                iterator.remove()
                result = true
            }
        }
        return result
    }

    override fun remove(element: T): Boolean {
        val iterator = iterator()
        while (iterator.hasNext()) {
            val currentElement = iterator.next()
            if (currentElement == element) {
                iterator.remove()
                return true
            }
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var removed = 0
        for (currentElement: T in elements) {
            removed += removeAll(currentElement)
        }
        return removed > 0
    }

    fun removeAll(element: T): Int {
        var removed = 0
        val iterator = iterator()
        while (iterator.hasNext()) {
            val currentElement = iterator.next()
            if (currentElement == element) {
                iterator.remove()
                removed += 1
            }
        }
        return removed
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append('[')
        result.append(asSequence().joinToString(", "))
        result.append(']')
        return result.toString()
    }

    abstract class AbstractArrayBasedListIterator<T>(
        protected val startIndex: Int = 0, protected var endIndex: Int, protected var nextIndex: Int = startIndex
    ) :
        MutableListIterator<T> {

        protected var callState: IteratorCallState = IteratorCallState.INIT

        enum class IteratorCallState {
            INIT, NEXT, PREV, ADD, REM, SET
        }

        override fun hasNext(): Boolean {
            return nextIndex in startIndex..<endIndex
        }

        override fun hasPrevious(): Boolean {
            return nextIndex in startIndex + 1..endIndex + 1
        }

        override fun nextIndex(): Int = nextIndex
        override fun previousIndex(): Int {
            return nextIndex - 1
        }

    }

}

/**
 * Checks the boundaries given by [startIndex] and [endIndex] with respect to the given [size].
 * @throws IndexOutOfBoundsException if any given index is out of bounds
 * @throws IllegalStateException if the [startIndex] is bigger than (or equal to) [endIndex]
 */
internal fun checkBounds(startIndex: Int, endIndex: Int, size: Int, initIndex: Int = startIndex) {
    if (startIndex !in 0..<size) {
        throw IndexOutOfBoundsException(startIndex)
    }
    if (endIndex !in 0..size) {
        throw IndexOutOfBoundsException(endIndex)
    }
    if (startIndex > endIndex) {
        throw IllegalArgumentException("Lower $startIndex index is bigger than upper index $endIndex.")
    }
    if (initIndex !in startIndex..endIndex) {
        throw IndexOutOfBoundsException(initIndex)
    }
}