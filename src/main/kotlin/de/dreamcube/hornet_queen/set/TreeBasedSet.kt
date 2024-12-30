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

package de.dreamcube.hornet_queen.set

import de.dreamcube.hornet_queen.*
import de.dreamcube.hornet_queen.tree.*
import java.util.*

abstract class TreeBasedSet<T>(private val binaryTree: PrimitiveTypeBinaryTree<T>, private val fastIterator: Boolean) : PrimitiveMutableSet<T> {

    override fun add(element: T): Boolean = binaryTree.insertKey(element) >= 0

    override val size: Int
        get() = binaryTree.size

    override fun clear() {
        binaryTree.markAsEmpty()
    }

    @Suppress("kotlin:S6529") // we are literally implementing isEmpty() here ... following the rule would cause endless recursion
    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> = if (fastIterator) unorderedIterator() else orderedIterator()

    /**
     * Provides an iterator that iterates the elements of this set in order of the comparator that was used to create this set. This is the default
     * iterator, if [fastIterator] is set to false (which is also the default if it is not specified). This iterator is comparable with the one
     * provided by [java.util.TreeSet], but slower.
     */
    fun orderedIterator(): MutableIterator<T> = OrderedTreeSetIterator(binaryTree)

    /**
     * Provides an iterator that iterates the elements of this set in no defined order. It is the default iterator, if [fastIterator] is set to true.
     * This iterator is way faster than the [orderedIterator] and also faster than the iterator of [java.util.TreeSet]. It simply iterates over the
     * internal array representation of this set.
     */
    fun unorderedIterator(): MutableIterator<T> = UnorderedTreeSetIterator(binaryTree)

    override fun remove(element: T): Boolean = binaryTree.removeKey(element) >= 0

    override fun contains(element: T): Boolean = binaryTree.containsKey(element)

    /**
     * Shrinks the internal array to the size of the set. This operation should only be called if no more elements are added to the list.
     */
    fun trimToSize() = binaryTree.trimToSize()

    internal class OrderedTreeSetIterator<T>(private val binaryTree: PrimitiveTypeBinaryTree<T>) : MutableIterator<T> {
        private val internalIterator: MutableIterator<Int> = binaryTree.inorderIndexIterator()

        override fun hasNext(): Boolean = internalIterator.hasNext()

        override fun next(): T = binaryTree.keys[internalIterator.next()]

        override fun remove() = internalIterator.remove()
    }

    internal class UnorderedTreeSetIterator<T>(private val binaryTree: PrimitiveTypeBinaryTree<T>) : MutableIterator<T> {
        private val internalIterator: MutableIterator<Int> = binaryTree.unorderedIndexIterator()

        override fun hasNext(): Boolean = internalIterator.hasNext()

        override fun next(): T = binaryTree.keys[internalIterator.next()]

        override fun remove() = internalIterator.remove()
    }
}

class PrimitiveByteTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Byte> = DEFAULT_BYTE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Byte>(PrimitiveByteBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class PrimitiveShortTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Short> = DEFAULT_SHORT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Short>(PrimitiveShortBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class PrimitiveCharTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Char> = DEFAULT_CHAR_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Char>(PrimitiveCharBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class PrimitiveIntTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Int> = DEFAULT_INT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Int>(PrimitiveIntBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class PrimitiveLongTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Long> = DEFAULT_LONG_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Long>(PrimitiveLongBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class PrimitiveFloatTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Float> = DEFAULT_FLOAT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Float>(PrimitiveFloatBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class PrimitiveDoubleTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<Double> = DEFAULT_DOUBLE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<Double>(PrimitiveDoubleBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)

class UUIDTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    fastIterator: Boolean = false,
    comparator: Comparator<UUID> = DEFAULT_UUID_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : TreeBasedSet<UUID>(UUIDBinaryTree(initialCapacity, false, comparator, native, maxHeightDifference), fastIterator)