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
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import java.util.*
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
    internal var keys: PrimitiveArray<K> = keyArraySupplier(initialSize)

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
            if (heightR(left[index]) < heightR(right[index])) {
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

    // TODO: actually implement it correctly, this is just for dev purposes
    fun isBalanced() = true // isBalanced(rootIndex)

    internal fun isBalanced(index: Int): Boolean {
        if (index == NO_INDEX) {
            return true
        }

        if (abs(heightR(left[index]) - heightR(right[index])) > 1) {
            return false
        }

        return isBalanced(left[index]) && isBalanced(right[index])
    }

    internal fun balance() {
        // TODO: this is highly inefficient in this implementation
        //balance(rootIndex)
    }

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
        if (heightR(leftIndex) > heightR(rightIndex)) {
            // left subtree is higher than the right one --> rotate the whole tree to the right and balance again
            val rightOfLeftIndex = right[leftIndex]
            val leftOfLeftIndex = left[leftIndex]
            if (heightR(rightOfLeftIndex) > heightR(leftOfLeftIndex)) {
                // the bigger subtree needs to be kept "outside"
                rotateLeft(leftIndex)
            }
            val newIndex = rotateRight(index)
            balance(newIndex)
        } else {
            // right subtree is higher than the left one --> rotate the whole tree to the left and balance again
            val leftOfRightIndex = left[rightIndex]
            val rightOfRightIndex = right[rightIndex]
            if (heightR(leftOfRightIndex) > heightR(rightOfRightIndex)) {
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

    fun height(): Int = heightI(rootIndex)

    internal fun heightR(index: Int) = fold(-1, index) { leftHeight: Int, _, rightHeight: Int ->
        1 + max(leftHeight, rightHeight)
    }

    fun toStringR(): String = fold("", rootIndex) { leftString: String, index: Int, rightString: String ->
        if (index == rootIndex)
            "($leftString [${keys[index]}] $rightString)"
        else
            "($leftString ${keys[index]} $rightString)"
    }

    internal fun traverseTree(index: Int, visitor: TreeVisitor<K>) {
        if (index == NO_INDEX) {
            return
        }

        val stack = PrimitiveIntArrayList()
        val visitedLeft = PrimitiveIntSetB()
        val visitedRight = PrimitiveIntSetB()
        val nodeProcessed = PrimitiveIntSetB()

        stack.add(index)
        while (stack.isNotEmpty()) {
            val currentIndex = stack[stack.lastIndex]
            val currentElement = keys[currentIndex]

            if (!visitedLeft.contains(currentIndex) && !visitedRight.contains(currentIndex)) {
                visitor.enterNode(currentElement, stack.size, currentIndex == index)
            }

            if (!visitedLeft.contains(currentIndex)) {
                visitedLeft.add(currentIndex)
                if (left[currentIndex] != NO_INDEX) {
                    stack.add(left[currentIndex])
                    continue
                }
            }

            if (!nodeProcessed.contains(currentIndex)) {
                visitor.visitNode(currentElement, stack.size, currentIndex == index)
                nodeProcessed.add(currentIndex)
            }

            if (!visitedRight.contains(currentIndex)) {
                visitedRight.add(currentIndex)
                if (right[currentIndex] != NO_INDEX) {
                    stack.add(right[currentIndex])
                    continue
                }
            }

            visitor.leaveNode(currentElement, stack.size, currentIndex == index)
            stack.removeLast()
        }
    }

    internal fun heightI(index: Int): Int {
        val visitor = HeightVisitor<K>()
        traverseTree(index, visitor)
        return visitor.height - 1
    }


    override fun toString(): String {
        val toStringVisitor = ToStringVisitor<K>()
        traverseTree(rootIndex, toStringVisitor)
        return toStringVisitor.result
    }

    interface TreeVisitor<K> {

        fun enterNode(node: K, depth: Int, root: Boolean) {
            // nothing by default
        }

        fun visitNode(node: K, depth: Int, root: Boolean) {
            // nothing by default
        }

        fun leaveNode(node: K, depth: Int, root: Boolean) {
            // nothing by default
        }

    }

    private class HeightVisitor<K> : TreeVisitor<K> {
        var height: Int = 0
            private set

        override fun enterNode(node: K, depth: Int, root: Boolean) {
            height = max(height, depth)
        }
    }

    private class ToStringVisitor<K> : TreeVisitor<K> {
        private val builder = StringBuilder()
        val result
            get() = builder.toString()

        override fun enterNode(node: K, depth: Int, root: Boolean) {
            builder.append("(")
        }

        override fun visitNode(node: K, depth: Int, root: Boolean) {
            if (root) {
                builder.append(" [$node] ")
            } else {
                builder.append(" $node ")
            }
        }

        override fun leaveNode(node: K, depth: Int, root: Boolean) {
            builder.append(")")
        }
    }

    internal fun inorderElements() {
        val iterator = BinaryTreeInorderIndexIterator()
        while (iterator.hasNext()) {
            val current = keys[iterator.next()]
            println(current)
        }
    }

    internal fun inorderIndexIterator(): MutableIterator<Int> = BinaryTreeInorderIndexIterator()

    internal fun unorderedIndexIterator(): MutableIterator<Int> = BinaryTreeUnorderedIndexIterator()

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

    internal inner class BinaryTreeDFSIndexIterator() :
        MutableIterator<Int> {
        /**
         * The top of the [stack] is the last index of the array list. We push by calling [MutableList.add] and we pop by calling
         * [MutableList.removeLast]. We also abuse the fact, that the [stack] is a list by removing indexes, if we have to remove something.
         */
        private val stack = PrimitiveIntArrayList()
        private val visitedLeft = PrimitiveIntSetB()
        private val visitedRight = PrimitiveIntSetB()
        private val nodeProcessed = PrimitiveIntSetB()
        private var iteratorChangeCount = changeCount
        private var lastDeliveredIndex = NO_INDEX
        private val currentPosition
            get() = if (stack.isEmpty()) NO_INDEX else stack[stack.lastIndex]

        init {
            if (rootIndex != NO_INDEX) {
                stack.add(rootIndex)
            }
            move()
        }

        private fun reset(position: Int) {
            stack.clear()
            visitedLeft.clear()
            visitedRight.clear()
            nodeProcessed.clear()
            iteratorChangeCount = changeCount
            if (rootIndex != NO_INDEX) {
                stack.add(rootIndex)
            }
            move()
            while (hasNext() && currentPosition != position) {
                next()
            }
            lastDeliveredIndex = NO_INDEX
        }

        private fun move() {
            // if we start with NO_INDEX, the stack will be empty and move won't do anything
            while (stack.isNotEmpty()) {
                if (!visitedLeft.contains(currentPosition)) {
                    visitedLeft.add(currentPosition)
                    if (left[currentPosition] != NO_INDEX) {
                        stack.add(left[currentPosition])
                        continue
                    }
                }

                if (!nodeProcessed.contains(currentPosition)) {
                    nodeProcessed.add(currentPosition)
                    // we have literally reached the next node for the iteration
                    // calling "move" again will resume after this if-Block,
                    // because the current node is still on top of the stack
                    return
                }

                if (!visitedRight.contains(currentPosition)) {
                    visitedRight.add(currentPosition)
                    if (right[currentPosition] != NO_INDEX) {
                        stack.add(right[currentPosition])
                        continue
                    }
                }

                // At this point, both the left and the right subtree have been processed, and we can finally remove the current node from the stack
                stack.removeLast()
            }
        }

        override fun hasNext(): Boolean = stack.isNotEmpty()

        override fun next(): Int {
            if (changeCount != iteratorChangeCount) {
                throw ConcurrentModificationException()
            }
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            lastDeliveredIndex = stack[stack.lastIndex]
            move()
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

            if (hasNext()) {
                if (currentPosition == size) {
                    // This case happens if the current Position was used to fill the gap after deletion
                    reset(lastDeliveredIndex)
                } else {
                    reset(currentPosition)
                }
            }
        }
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