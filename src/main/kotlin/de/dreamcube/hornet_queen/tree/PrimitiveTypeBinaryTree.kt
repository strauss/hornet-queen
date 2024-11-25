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

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.NO_INDEX
import de.dreamcube.hornet_queen.array.PrimitiveArray
import de.dreamcube.hornet_queen.array.PrimitiveIntArray
import kotlin.math.abs
import kotlin.math.max

abstract class PrimitiveTypeBinaryTree<K> protected constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    private val allowDuplicateKeys: Boolean,
    keyArraySupplier: (Int) -> PrimitiveArray<K>,
    private val comparator: Comparator<K>
) {
    /**
     * Contains the actual keys.
     */
    private var keys: PrimitiveArray<K> = keyArraySupplier(initialSize)

    /**
     * Contains the left child of every node.
     */
    internal var left = PrimitiveIntArray(initialSize, keys.native)
        private set

    /**
     * Contains the right child of every node.
     */
    internal var right = PrimitiveIntArray(initialSize, keys.native)
        private set

    /**
     * Contains the parent of every node.
     */
    internal var parent = PrimitiveIntArray(initialSize, keys.native)
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

    private fun markAsEmpty() {
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
        if (left[parent[index]] == index) {
            left[parent[index]] = NO_INDEX
        }
        if (right[parent[index]] == index) {
            right[parent[index]] = NO_INDEX
        }
        if (size == 0) {
            markAsEmpty()
            return
        }
        if (index == size) {
            balance()
            return
        }
        // Fill the gap
        keys[index] = keys[size]
        if (size == rootIndex) {
            rootIndex = index
        } else {
            if (left[parent[size]] == size) {
                left[parent[size]] = index
            }
            if (right[parent[size]] == size) {
                right[parent[size]] = index
            }
        }
        left[index] = left[size]
        right[index] = right[size]
        parent[index] = parent[size]
        left[size] = NO_INDEX
        right[size] = NO_INDEX
        parent[size] = NO_INDEX
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
        changeCount += 1
        if (fixLeft) {
            left[originalParent] = indexOther
        }
        if (fixRight) {
            right[originalParent] = indexOther
        }
        return indexOther
    }

    fun isBalanced() = isBalanced(rootIndex)

    internal fun isBalanced(index: Int): Boolean {
        if (index == NO_INDEX) {
            return true
        }

        if (abs(height(left[index]) - height(right[index])) > 1) {
            return false
        }

        return isBalanced(left[index]) && isBalanced(right[index])
    }

    internal fun balance() = balance(rootIndex)

    private fun balance(index: Int) {
        if (isBalanced(index)) {
            return
        }
        val leftIndex = left[index]
        val rightIndex = right[index]
        if (!isBalanced(leftIndex)) {
            balance(leftIndex)
        }
        if (!isBalanced(rightIndex)) {
            balance(rightIndex)
        }
        // if both are balanced, the height difference is significant, and we need to adjust for that
        if (height(leftIndex) > height(rightIndex)) {
            // left subtree is higher than the right one --> rotate the whole tree to the right and balance again
            val rightOfLeftIndex = right[leftIndex]
            val leftOfLeftIndex = left[leftIndex]
            if (height(rightOfLeftIndex) > height(leftOfLeftIndex)) {
                // the bigger subtree needs to be kept "outside"
                rotateLeft(leftIndex)
            }
            val newIndex = rotateRight(index)
            balance(newIndex)
        } else {
            // right subtree is higher than the left one --> rotate the whole tree to the left and balance again
            val leftOfRightIndex = left[rightIndex]
            val rightOfRightIndex = right[rightIndex]
            if (height(leftOfRightIndex) > height(rightOfRightIndex)) {
                // the bigger subtree needs to be kept "outside"
                rotateRight(rightIndex)
            }
            val newIndex = rotateLeft(index)
            balance(newIndex)
        }
    }

    /**
     * Folding trees has always been a pleasure :-). The given function [f] is applied to the recursive result of the left and the right subtree.
     * The [neutralElement] is the result of an empty subtree.
     */
    internal fun <N> fold(neutralElement: N, index: Int, f: (N, Int, N) -> N): N = if (index == NO_INDEX) neutralElement else
        f(fold(neutralElement, left[index], f), index, fold(neutralElement, right[index], f))

    fun height(): Int = height(rootIndex)

    internal fun height(index: Int) = fold(-1, index) { leftHeight: Int, _, rightHeight: Int ->
        1 + max(leftHeight, rightHeight)
    }

    override fun toString(): String = fold("", rootIndex) { leftString: String, index: Int, rightString: String ->
        if (index == rootIndex)
            "($leftString [${keys[index]}] $rightString)"
        else
            "($leftString ${keys[index]} $rightString)"
    }

    internal fun inorderElements() {
        val iterator = BinaryTreeInorderIndexIterator()
        while (iterator.hasNext()) {
            val current = keys[iterator.next()]
            println(current)
        }
    }

    /**
     * Inserts the given [key] to this binary tree. Returns [NO_INDEX] if duplicates are forbidden and it is already contained.
     */
    fun insertKey(key: K): Int {
        if (rootIndex == NO_INDEX) {
            // first insert
            rootIndex = size
            size += 1
            keys[rootIndex] = key
            parent[rootIndex] = NO_INDEX
            left[rootIndex] = NO_INDEX
            right[rootIndex] = NO_INDEX
            changeCount += 1
            return rootIndex
        }
        val insertKey = insertKey(key, rootIndex, NO_INDEX)
        if (insertKey != NO_INDEX) {
            balance()
        }
        return insertKey
    }

    private fun insertKey(key: K, index: Int, parentIndex: Int): Int {
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
            insertAt = insertKey(key, leftIndex, index)
            if (leftIndex == NO_INDEX) {
                left[index] = insertAt
            }
        } else {
            val rightIndex = right[index]
            insertAt = insertKey(key, rightIndex, index)
            if (rightIndex == NO_INDEX) {
                right[index] = insertAt
            }
        }
        return insertAt
    }

    private fun grow() {
        val oldCapacity: Int = keys.size
        val newCapacity: Int = keys.calculateSizeForGrow()
        val delta: Int = newCapacity - oldCapacity
        keys = keys.getResizedCopy(delta)
        left = left.getResizedCopy(delta)
        right = right.getResizedCopy(delta)
        parent = parent.getResizedCopy(delta)
    }

    fun trimToSize() {
        val difference = size - keys.size
        keys = keys.getResizedCopy(difference)
        left = left.getResizedCopy(difference)
        right = right.getResizedCopy(difference)
        parent = parent.getResizedCopy(difference)
    }

    internal inner class BinaryTreeInorderIndexIterator : Iterator<Int> {
        private var currentPosition: Int = rootIndex
        private val iteratorChangeCount = changeCount

        private var returnedElements = 0

        init {
            dropLeft()
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
            } else {
                if (currentPosition == right[parent[currentPosition]]) {

                    // If we have finished a right branch, we need to raise one layer up until the finished branch becomes a left one.
                    while (currentPosition != rootIndex && currentPosition == right[parent[currentPosition]]) {
                        currentPosition = parent[currentPosition]
                    }
                }
                // If we just finished a left branch, we only need to go up one layer. That element will be the next one.
                currentPosition = parent[currentPosition]

            }
        }

        override fun next(): Int {
            if (changeCount != iteratorChangeCount) {
                throw ConcurrentModificationException()
            }
            val result: Int = currentPosition
            returnedElements += 1
            seekRight()
            return result
        }

    }

}


class PrimitiveIntBinaryTree @JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Int> = Comparator { a: Int, b: Int -> a.compareTo(b) },
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeBinaryTree<Int>(initialSize, allowDuplicates, { size: Int -> PrimitiveIntArray(size, native) }, comparator)