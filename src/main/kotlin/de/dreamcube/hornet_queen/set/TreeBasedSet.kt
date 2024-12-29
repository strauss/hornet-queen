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

abstract class TreeBasedSet<T>(private val binaryTree: PrimitiveTypeBinaryTree<T>) : PrimitiveMutableSet<T> {

    override fun add(element: T): Boolean = binaryTree.insertKey(element) >= 0

    override val size: Int
        get() = binaryTree.size

    override fun clear() {
        binaryTree.markAsEmpty()
    }

    @Suppress("kotlin:S6529") // we are literally implementing isEmpty() here ... following the rule would cause endless recursion
    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> = OrderedTreeSetIterator(binaryTree)

    fun fastIterator(): MutableIterator<T> = UnorderedTreeSetIterator(binaryTree)

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
    comparator: Comparator<Byte> = DEFAULT_BYTE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Byte>(PrimitiveByteBinaryTree(initialCapacity, false, comparator, native))

class PrimitiveShortTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<Short> = DEFAULT_SHORT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Short>(PrimitiveShortBinaryTree(initialCapacity, false, comparator, native))

class PrimitiveCharTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<Char> = DEFAULT_CHAR_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Char>(PrimitiveCharBinaryTree(initialCapacity, false, comparator, native))

class PrimitiveIntTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<Int> = DEFAULT_INT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Int>(PrimitiveIntBinaryTree(initialCapacity, false, comparator, native))

class PrimitiveLongTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<Long> = DEFAULT_LONG_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Long>(PrimitiveLongBinaryTree(initialCapacity, false, comparator, native))

class PrimitiveFloatTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<Float> = DEFAULT_FLOAT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Float>(PrimitiveFloatBinaryTree(initialCapacity, false, comparator, native))

class PrimitiveDoubleTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<Double> = DEFAULT_DOUBLE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<Double>(PrimitiveDoubleBinaryTree(initialCapacity, false, comparator, native))

class UUIDTreeSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<UUID> = DEFAULT_UUID_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : TreeBasedSet<UUID>(UUIDBinaryTree(initialCapacity, false, comparator, native))