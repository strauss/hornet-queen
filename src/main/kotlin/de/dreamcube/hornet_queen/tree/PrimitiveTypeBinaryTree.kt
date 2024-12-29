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

package de.dreamcube.hornet_queen.tree

import de.dreamcube.hornet_queen.*
import de.dreamcube.hornet_queen.array.*
import java.util.*

abstract class PrimitiveTypeBinaryTree<K> protected constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    private val allowDuplicateKeys: Boolean,
    keyArraySupplier: (Int) -> PrimitiveArray<K>,
    private val comparator: Comparator<K>
) {
    /**
     * Contains the actual keys.
     */
    internal var keys: PrimitiveArray<K> = keyArraySupplier(initialSize)

    /**
     * Contains the index of the left child of every node.
     */
    internal var left = PrimitiveIntArray(initialSize, keys.native)
        private set

    /**
     * Contains the index of the right child of every node.
     */
    internal var right = PrimitiveIntArray(initialSize, keys.native)
        private set

    /**
     * Contains the index of the parent of every node.
     */
    internal var parent = PrimitiveIntArray(initialSize, keys.native)
        private set

    /**
     * Contains the height information for every node. We are using [Byte] because the maximum height is 31.
     */
    internal var height = PrimitiveByteArray(initialSize, keys.native)
        private set

    /**
     * Contains the index of the current root node.
     */
    internal var rootIndex: Int = NO_INDEX
        private set

    /**
     * The size of the tree ... also the index of the next free node.
     */
    var size: Int = 0
        private set

    /**
     * Internal variable for indicating changes. This is required for all iterators.
     */
    private var changeCount: Int = 0

    init {
        markAsEmpty()
    }

    internal fun markAsEmpty() {
        size = 0
        rootIndex = NO_INDEX
    }

    /**
     * Searches for the given [key] and returns its index. If the given [key] is not found, [NO_INDEX] is returned.
     */
    internal fun searchKey(key: K): Int {
        return searchKey(key, rootIndex)
    }

    private tailrec fun searchKey(key: K, index: Int): Int {
        if (index == NO_INDEX) {
            return -1
        }
        val currentKey = keys[index]
        val compareResult = comparator.compare(key, currentKey)
        if (compareResult == 0) {
            return index
        }
        return if (compareResult < 0) {
            searchKey(key, left[index])
        } else {
            searchKey(key, right[index])
        }
    }

    /**
     * Checks if the given [key] is contained in the tree.
     */
    fun containsKey(key: K): Boolean = searchKey(key) >= 0

    /**
     * Removes the given [key] from the tree and returns the index it was removed from. If it was not found, nothing is removed and [NO_INDEX] is
     * returned.
     */
    fun removeKey(key: K): Int {
        val index = searchKey(key)
        if (index >= 0) {
            removeKeyAt(index)
        }
        return index
    }

    private fun removeKeyAt(index: Int) {
        // for removal, we rotate until the element is a leaf
        while (!isLeaf(index)) {
            if (height(left[index]) < height(right[index])) {
                rotateLeft(index)
            } else {
                rotateRight(index)
            }
        }
        size -= 1
        if (index == rootIndex) {
            rootIndex = NO_INDEX
            return
        }
        val parentOfIndex = parent[index]
        if (left[parentOfIndex] == index) {
            left[parentOfIndex] = NO_INDEX
        }
        if (right[parentOfIndex] == index) {
            right[parentOfIndex] = NO_INDEX
        }
        if (size == 0) {
            markAsEmpty()
            return
        }
        if (index == size) {
            fixHeightsBottomUp(index)
            balance()
            return
        }
        // Fill the gap
        keys[index] = keys[size]
        if (size == rootIndex) {
            rootIndex = index
        } else {
            // If the moved key is its parent's left element, adjust the left reference of the parent
            if (left[parent[size]] == size) {
                left[parent[size]] = index
            }
            // If the moved key is its parent's right element, adjust the right reference of the parent
            if (right[parent[size]] == size) {
                right[parent[size]] = index
            }
        }
        left[index] = left[size]
        right[index] = right[size]
        parent[index] = parent[size]
        height[index] = height[size]
        // Fix parent reference for left and right
        if (left[index] != NO_INDEX) {
            parent[left[index]] = index
        }
        if (right[index] != NO_INDEX) {
            parent[right[index]] = index
        }
        left[size] = NO_INDEX
        right[size] = NO_INDEX
        parent[size] = NO_INDEX
        height[size] = 0
        // if the last index happens to be the parent of the removed index, we can be sure that "index" itself is its former parent
        val fixIndex = if (parentOfIndex == size) index else parentOfIndex
        fixHeightsBottomUp(fixIndex)
        balance()
    }

    private fun isLeaf(index: Int): Boolean = index != NO_INDEX && left[index] == NO_INDEX && right[index] == NO_INDEX

    internal fun rotateLeft(index: Int) = internalRotate(index, left, right)

    internal fun rotateRight(index: Int) = internalRotate(index, right, left)

    private fun internalRotate(index: Int, directionArray: PrimitiveIntArray, otherArray: PrimitiveIntArray): Int {
        if (index == NO_INDEX || otherArray[index] == NO_INDEX) {
            // If there is no subtree on the other side, a rotation is not possible
            return index
        }
        val fixLeft = parent[index] != NO_INDEX && left[parent[index]] == index
        val fixRight = parent[index] != NO_INDEX && right[parent[index]] == index
        val originalParent = parent[index]
        // index of other subtree
        val indexOther = otherArray[index]
        // index of right subtree's left subtree
        val indexDirectionOfOther = directionArray[indexOther]
        parent[indexOther] = parent[index]
        directionArray[indexOther] = index
        parent[index] = indexOther
        otherArray[index] = indexDirectionOfOther
        if (indexDirectionOfOther != NO_INDEX) {
            parent[indexDirectionOfOther] = index
        }
        if (index == rootIndex) {
            rootIndex = indexOther
        }
        if (fixLeft) {
            left[originalParent] = indexOther
        }
        if (fixRight) {
            right[originalParent] = indexOther
        }

        // fix heights
        height[index] = determineNewHeight(index)
        height[indexOther] = determineNewHeight(indexOther)

        changeCount += 1
        return indexOther
    }

    private fun determineNewHeight(index: Int): Byte = when {
        index == NO_INDEX -> -1
        left[index] == NO_INDEX && right[index] == NO_INDEX -> 0
        left[index] == NO_INDEX -> height[right[index]].inc()
        right[index] == NO_INDEX -> height[left[index]].inc()
        else -> max(height[left[index]], height[right[index]]).inc()
    }

    internal fun balance() {
        // TODO: this is highly inefficient in this implementation
        //balance(rootIndex)
    }

    fun height(): Int = if (rootIndex == NO_INDEX) NO_INDEX else height[rootIndex].toInt()

    internal fun height(index: Int): Byte = if (index == NO_INDEX) -1 else height[index]

    override fun toString() = toStringR()

    internal fun inorderIndexIterator(): MutableIterator<Int> = BinaryTreeInorderIndexIterator()

    internal fun unorderedIndexIterator(): MutableIterator<Int> = BinaryTreeUnorderedIndexIterator()

    /**
     * Inserts the given [key] to this binary tree. Returns [NO_INDEX] if duplicates are forbidden and it is already contained.
     */
    fun insertKey(key: K): Int {
        if (rootIndex == NO_INDEX) {
            // first insert
            rootIndex = 0
            internalAdd(key, NO_INDEX)
            return rootIndex
        }

        var currentIndex = rootIndex

        // this loop searches the tree without using recursion
        while (true) {
            val currentKey = keys[currentIndex]
            val compareResult = comparator.compare(key, currentKey)
            if (!allowDuplicateKeys && compareResult == 0) {
                // No duplicates allowed
                return NO_INDEX
            }
            if (compareResult < 0) {
                val leftIndex = left[currentIndex]
                if (leftIndex == NO_INDEX) {
                    left[currentIndex] = size
                    internalAdd(key, currentIndex)
                    break
                } else {
                    currentIndex = leftIndex
                    continue
                }
            }
            if (compareResult > 0) {
                val rightIndex = right[currentIndex]
                if (rightIndex == NO_INDEX) {
                    right[currentIndex] = size
                    internalAdd(key, currentIndex)
                    break
                } else {
                    currentIndex = rightIndex
                    continue
                }
            }
        }
        // update height
        val fixIndex = size - 1
        fixHeightsBottomUp(fixIndex)
        // TODO: balance up, starting at size - 1
        return size - 1
    }

    private fun fixHeightsBottomUp(fixIndex: Int) {
        var previousIndex = fixIndex
        if (isLeaf(fixIndex)) {
            height[fixIndex] = 0
        }
        while (previousIndex != rootIndex) {
            val index = parent[previousIndex]
            height[index] = determineNewHeight(index)
            previousIndex = index
        }
    }

    private fun internalAdd(key: K, parentIndex: Int) {
        // we do the growth check here for avoiding unnecessary growing if a forbidden duplicate is tried to be inserted
        if (size == keys.size) {
            grow()
        }
        keys[size] = key
        parent[size] = parentIndex
        left[size] = NO_INDEX
        right[size] = NO_INDEX
        height[size] = 0
        changeCount += 1
        size += 1
    }

    /**
     * For some reason, max is not defined for [Byte].
     */
    private fun max(a: Byte, b: Byte): Byte = if (a > b) a else b

    private fun insertKeyR(key: K, index: Int, parentIndex: Int): Int {
        if (index == NO_INDEX) {
            // we do the growth check here for avoiding unnecessary growing if a forbidden duplicate is tried to be inserted
            if (size == keys.size) {
                grow()
            }
            val insertAt = size
            keys[insertAt] = key
            size += 1
            parent[insertAt] = parentIndex
            left[insertAt] = NO_INDEX
            right[insertAt] = NO_INDEX
            changeCount += 1
            return insertAt
        }
        val currentKey = keys[index]
        val insertAt: Int
        val compareResult = comparator.compare(key, currentKey)
        if (!allowDuplicateKeys && compareResult == 0) {
            // No duplicates allowed
            return NO_INDEX
        }
        if (compareResult < 0) {
            val leftIndex = left[index]
            insertAt = insertKeyR(key, leftIndex, index)
            if (leftIndex == NO_INDEX) {
                left[index] = insertAt
            }
        } else {
            val rightIndex = right[index]
            insertAt = insertKeyR(key, rightIndex, index)
            if (rightIndex == NO_INDEX) {
                right[index] = insertAt
            }
        }
        return insertAt
    }

    fun insertKeyR(key: K): Int {
        if (rootIndex == NO_INDEX) {
            // first insert
            rootIndex = size
            size += 1
            keys[rootIndex] = key
            parent[rootIndex] = NO_INDEX
            left[rootIndex] = NO_INDEX
            right[rootIndex] = NO_INDEX
            height[rootIndex] = 0
            changeCount += 1
            return rootIndex
        }
        val insertKey = insertKeyR(key, rootIndex, NO_INDEX)
        if (insertKey != NO_INDEX) {
            balance()
        }
        return insertKey
    }

    private fun grow() {
        val oldCapacity: Int = keys.size
        val newCapacity: Int = keys.calculateSizeForGrow()
        val delta: Int = newCapacity - oldCapacity
        keys = keys.getResizedCopy(delta)
        left = left.getResizedCopy(delta)
        right = right.getResizedCopy(delta)
        parent = parent.getResizedCopy(delta)
        height = height.getResizedCopy(delta)
    }

    fun trimToSize() {
        val difference = size - keys.size
        keys = keys.getResizedCopy(difference)
        left = left.getResizedCopy(difference)
        right = right.getResizedCopy(difference)
        parent = parent.getResizedCopy(difference)
    }

    internal inner class BinaryTreeInorderIndexIterator : MutableIterator<Int> {
        private var currentPosition: Int
        private var iteratorChangeCount: Int
        private var lastDeliveredIndex: Int
        private var returnedElements: Int

        init {
            currentPosition = rootIndex
            iteratorChangeCount = changeCount
            lastDeliveredIndex = NO_INDEX
            returnedElements = 0
            dropLeft()
        }

        private fun reset(position: Int) {
            currentPosition = rootIndex
            iteratorChangeCount = changeCount
            returnedElements = 0
            dropLeft()
            while (hasNext() && currentPosition != position) {
                next()
            }
            lastDeliveredIndex = NO_INDEX
        }

        override fun hasNext(): Boolean = returnedElements < size

        private fun dropLeft() {
            if (currentPosition == NO_INDEX) {
                return
            }
            while (left[currentPosition] != NO_INDEX) {
                currentPosition = left[currentPosition]
            }
        }

        private fun seekRight() {
            if (right[currentPosition] != NO_INDEX) {
                currentPosition = right[currentPosition]
                dropLeft()
            } else if (parent[currentPosition] != NO_INDEX) {
                if (currentPosition == right[parent[currentPosition]]) {

                    // If we have finished a right branch, we need to raise one layer up until the finished branch becomes a left one.
                    while (currentPosition != rootIndex && currentPosition == right[parent[currentPosition]]) {
                        currentPosition = parent[currentPosition]
                    }
                }
                // If we just finished a left branch, we only need to go up one layer. That element will be the next one.
                currentPosition = parent[currentPosition]
            } else {
                // Special case: Root node is last element, therefore no parent and no right branch
                currentPosition = NO_INDEX
            }
        }

        override fun next(): Int {
            if (changeCount != iteratorChangeCount) {
                throw ConcurrentModificationException()
            }
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            lastDeliveredIndex = currentPosition
            returnedElements += 1
            seekRight()
            return lastDeliveredIndex
        }

        override fun remove() {
            if (changeCount != iteratorChangeCount) {
                throw ConcurrentModificationException()
            }
            if (lastDeliveredIndex == NO_INDEX) {
                throw IllegalStateException()
            }
            this@PrimitiveTypeBinaryTree.removeKeyAt(lastDeliveredIndex)
            // This case happens if the current Position was used to fill the gap after deletion
            if (currentPosition == size) {
                currentPosition = lastDeliveredIndex
            }
            returnedElements -= 1
            if (hasNext()) {
                reset(currentPosition)
            }
        }

    }

    internal inner class BinaryTreeUnorderedIndexIterator : MutableIterator<Int> {
        private var iteratorChangeCount: Int = changeCount
        private var currentPosition = 0
        private var lastDeliveredIndex: Int = NO_INDEX

        override fun hasNext(): Boolean = currentPosition < size
        override fun next(): Int {
            if (changeCount != iteratorChangeCount) {
                throw ConcurrentModificationException()
            }
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            lastDeliveredIndex = currentPosition
            currentPosition += 1
            return lastDeliveredIndex
        }

        override fun remove() {
            if (changeCount != iteratorChangeCount) {
                throw ConcurrentModificationException()
            }
            if (lastDeliveredIndex == NO_INDEX) {
                throw IllegalStateException()
            }
            this@PrimitiveTypeBinaryTree.removeKeyAt(lastDeliveredIndex)
            // This case happens if the current Position was used to fill the gap after deletion
            if (currentPosition == size) {
                currentPosition = lastDeliveredIndex
            }
            currentPosition -= 1
            if (hasNext()) {
                iteratorChangeCount = changeCount
            }
        }
    }

}

class PrimitiveByteBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Byte> = DEFAULT_BYTE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Byte>(initialSize, allowDuplicates, { size: Int -> PrimitiveByteArray(size, native) }, comparator)

class PrimitiveShortBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Short> = DEFAULT_SHORT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Short>(initialSize, allowDuplicates, { size: Int -> PrimitiveShortArray(size, native) }, comparator)

class PrimitiveCharBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Char> = DEFAULT_CHAR_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Char>(initialSize, allowDuplicates, { size: Int -> PrimitiveCharArray(size, native) }, comparator)

class PrimitiveIntBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Int> = DEFAULT_INT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Int>(initialSize, allowDuplicates, { size: Int -> PrimitiveIntArray(size, native) }, comparator)

class PrimitiveLongBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Long> = DEFAULT_LONG_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Long>(initialSize, allowDuplicates, { size: Int -> PrimitiveLongArray(size, native) }, comparator)

class PrimitiveFloatBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Float> = DEFAULT_FLOAT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Float>(initialSize, allowDuplicates, { size: Int -> PrimitiveFloatArray(size, native) }, comparator)

class PrimitiveDoubleBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Double> = DEFAULT_DOUBLE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Double>(initialSize, allowDuplicates, { size: Int -> PrimitiveDoubleArray(size, native) }, comparator)

class UUIDBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<UUID> = DEFAULT_UUID_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<UUID>(initialSize, allowDuplicates, { size: Int -> UUIDArray(size, native) }, comparator)