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
import kotlin.math.abs

abstract class PrimitiveTypeBinaryTree<K> protected constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    private val allowDuplicateKeys: Boolean,
    keyArraySupplier: (Int) -> PrimitiveArray<K>,
    private val comparator: Comparator<K>,
    val maxHeightDifference: Byte
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

    private tailrec fun searchKey(key: K, index: Int, containsCheck: Boolean = true): Int {
        if (index == NO_INDEX) {
            return if (containsCheck) -1 else index
        }
        val currentKey = keys[index]
        val compareResult = comparator.compare(key, currentKey)
        if (compareResult == 0) {
            return index
        }
        return if (compareResult < 0) {
            searchKey(key, left.getP(index))
        } else {
            searchKey(key, right.getP(index))
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
            if (height(left.getP(index)) < height(right.getP(index))) {
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
        val parentOfIndex = parent.getP(index)
        if (left.getP(parentOfIndex) == index) {
            left.setP(parentOfIndex, NO_INDEX)
        }
        if (right.getP(parentOfIndex) == index) {
            right.setP(parentOfIndex, NO_INDEX)
        }
        if (size == 0) {
            markAsEmpty()
            return
        }
        if (index == size) {
            balanceUp(parentOfIndex)
            return
        }
        // Fill the gap
        keys[index] = keys[size]
        if (size == rootIndex) {
            rootIndex = index
        } else {
            // If the moved key is its parent's left element, adjust the left reference of the parent
            if (left.getP(parent.getP(size)) == size) {
                left.setP(parent.getP(size), index)
            }
            // If the moved key is its parent's right element, adjust the right reference of the parent
            if (right.getP(parent.getP(size)) == size) {
                right.setP(parent.getP(size), index)
            }
        }
        left.setP(index, left.getP(size))
        right.setP(index, right.getP(size))
        parent.setP(index, parent.getP(size))
        height.setP(index, height.getP(size))
        // Fix parent reference for left and right
        if (left.getP(index) != NO_INDEX) {
            parent.setP(left.getP(index), index)
        }
        if (right.getP(index) != NO_INDEX) {
            parent.setP(right.getP(index), index)
        }
        left.setP(size, NO_INDEX)
        right.setP(size, NO_INDEX)
        parent.setP(size, NO_INDEX)
        height.setP(size, 0)
        // if the last index happens to be the parent of the removed index, we can be sure that "index" itself is its former parent
        val fixIndex = if (parentOfIndex == size) index else parentOfIndex
        balanceUp(fixIndex)
    }

    private fun isLeaf(index: Int): Boolean = index != NO_INDEX && left.getP(index) == NO_INDEX && right.getP(index) == NO_INDEX

    internal fun rotateLeft(index: Int) = internalRotate(index, left, right)

    internal fun rotateRight(index: Int) = internalRotate(index, right, left)

    private fun internalRotate(index: Int, directionArray: PrimitiveIntArray, otherArray: PrimitiveIntArray): Int {
        if (index == NO_INDEX || otherArray.getP(index) == NO_INDEX) {
            // If there is no subtree on the other side, a rotation is not possible
            return index
        }
        val fixLeft = parent.getP(index) != NO_INDEX && left.getP(parent.getP(index)) == index
        val fixRight = parent.getP(index) != NO_INDEX && right.getP(parent.getP(index)) == index
        val originalParent = parent.getP(index)
        // index of other subtree
        val indexOther = otherArray.getP(index)
        // index of right subtree's left subtree
        val indexDirectionOfOther = directionArray.getP(indexOther)
        parent.setP(indexOther, parent.getP(index))
        directionArray.setP(indexOther, index)
        parent.setP(index, indexOther)
        otherArray.setP(index, indexDirectionOfOther)
        if (indexDirectionOfOther != NO_INDEX) {
            parent.setP(indexDirectionOfOther, index)
        }
        if (index == rootIndex) {
            rootIndex = indexOther
        }
        if (fixLeft) {
            left.setP(originalParent, indexOther)
        }
        if (fixRight) {
            right.setP(originalParent, indexOther)
        }

        // fix heights
        height.setP(index, determineNewHeight(index))
        height.setP(indexOther, determineNewHeight(indexOther))

        changeCount += 1
        return indexOther
    }

    private fun determineNewHeight(index: Int): Byte = when {
        index == NO_INDEX -> -1
        left.getP(index) == NO_INDEX && right.getP(index) == NO_INDEX -> 0
        left.getP(index) == NO_INDEX -> height.getP(right.getP(index)).inc()
        right.getP(index) == NO_INDEX -> height.getP(left.getP(index)).inc()
        else -> max(height.getP(left.getP(index)), height.getP(right.getP(index))).inc()
    }

    private fun balanceUp(index: Int) {
        var currentIndex: Int = index
        while (currentIndex != NO_INDEX) {
            if (locallyBalanced(currentIndex)) {
                height.setP(currentIndex, determineNewHeight(currentIndex))
                currentIndex = parent.getP(currentIndex)
                continue
            }
            val rightIndex = right.getP(currentIndex)
            val leftIndex = left.getP(currentIndex)
            if (height(leftIndex) < height(rightIndex)) {
                val leftOfRightIndex = left.getP(rightIndex)
                val rightOfRightIndex = right.getP(rightIndex)
                if (height(leftOfRightIndex) > height(rightOfRightIndex)) {
                    // keep bigger subtree outside -> double rotation
                    rotateRight(rightIndex)
                }
                rotateLeft(currentIndex)
            } else {
                // right < left
                val rightOfLeftIndex = right.getP(leftIndex)
                val leftOfLeftIndex = left.getP(leftIndex)
                if (height(rightOfLeftIndex) > height(leftOfLeftIndex)) {
                    // keep bigger subtree outside -> double rotation
                    rotateRight(leftIndex)
                }
                rotateRight(currentIndex)
            }
        }
    }

    private fun locallyBalanced(index: Int): Boolean {
        val leftHeight = height(left.getP(index))
        val rightHeight = height(right.getP(index))
        return maxHeightDifference <= 0 || abs(leftHeight - rightHeight) <= maxHeightDifference
    }

    fun height(): Int = if (rootIndex == NO_INDEX) NO_INDEX else height.getP(rootIndex).toInt()

    internal fun height(index: Int): Byte = if (index == NO_INDEX) -1 else height.getP(index)

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
                val leftIndex = left.getP(currentIndex)
                if (leftIndex == NO_INDEX) {
                    left.setP(currentIndex, size)
                    internalAdd(key, currentIndex)
                    break
                } else {
                    currentIndex = leftIndex
                    continue
                }
            }
            if (compareResult > 0) {
                val rightIndex = right.getP(currentIndex)
                if (rightIndex == NO_INDEX) {
                    right.setP(currentIndex, size)
                    internalAdd(key, currentIndex)
                    break
                } else {
                    currentIndex = rightIndex
                    continue
                }
            }
        }
        val fixIndex = size - 1
        balanceUp(fixIndex)
        return fixIndex
    }

    private fun internalAdd(key: K, parentIndex: Int) {
        // we do the growth check here for avoiding unnecessary growing if a forbidden duplicate is tried to be inserted
        if (size == keys.size) {
            grow()
        }
        keys[size] = key
        parent.setP(size, parentIndex)
        left.setP(size, NO_INDEX)
        right.setP(size, NO_INDEX)
        height.setP(size, 0)
        changeCount += 1
        size += 1
    }

    /**
     * For some reason, max is not defined for [Byte].
     */
    private fun max(a: Byte, b: Byte): Byte = if (a > b) a else b

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
            while (left.getP(currentPosition) != NO_INDEX) {
                currentPosition = left.getP(currentPosition)
            }
        }

        private fun seekRight() {
            if (right.getP(currentPosition) != NO_INDEX) {
                currentPosition = right.getP(currentPosition)
                dropLeft()
            } else if (parent.getP(currentPosition) != NO_INDEX) {
                if (currentPosition == right.getP(parent.getP(currentPosition))) {

                    // If we have finished a right branch, we need to raise one layer up until the finished branch becomes a left one.
                    while (currentPosition != rootIndex && currentPosition == right.getP(parent.getP(currentPosition))) {
                        currentPosition = parent.getP(currentPosition)
                    }
                }
                // If we just finished a left branch, we only need to go up one layer. That element will be the next one.
                currentPosition = parent.getP(currentPosition)
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
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Byte>(initialSize, allowDuplicates, { size: Int -> PrimitiveByteArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveShortBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Short> = DEFAULT_SHORT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Short>(initialSize, allowDuplicates, { size: Int -> PrimitiveShortArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveCharBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Char> = DEFAULT_CHAR_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Char>(initialSize, allowDuplicates, { size: Int -> PrimitiveCharArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveIntBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Int> = DEFAULT_INT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Int>(initialSize, allowDuplicates, { size: Int -> PrimitiveIntArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveLongBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Long> = DEFAULT_LONG_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Long>(initialSize, allowDuplicates, { size: Int -> PrimitiveLongArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveFloatBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Float> = DEFAULT_FLOAT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Float>(initialSize, allowDuplicates, { size: Int -> PrimitiveFloatArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveDoubleBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Double> = DEFAULT_DOUBLE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Double>(
    initialSize,
    allowDuplicates,
    { size: Int -> PrimitiveDoubleArray(size, native) },
    comparator,
    maxHeightDifference
)

class UUIDBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<UUID> = DEFAULT_UUID_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<UUID>(initialSize, allowDuplicates, { size: Int -> UUIDArray(size, native) }, comparator, maxHeightDifference)