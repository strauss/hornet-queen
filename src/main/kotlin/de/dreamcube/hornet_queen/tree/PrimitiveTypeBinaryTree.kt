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
import de.dreamcube.hornet_queen.shared.MutableIndexedValueCollection
import java.util.*
import kotlin.math.abs

abstract class PrimitiveTypeBinaryTree<K, V> protected constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    private val allowDuplicateKeys: Boolean,
    keyArraySupplier: (Int) -> PrimitiveArray<K>,
    private val comparator: Comparator<K>,
    maxHeightDifference: Byte,
    private val valuesSupplier: ((Int) -> MutableIndexedValueCollection<V>)? = null
) {
    internal val maxHeightDifference = when {
        maxHeightDifference > 5 -> 5
        maxHeightDifference >= 1 -> maxHeightDifference
        maxHeightDifference < 0 -> -1
        else -> 1 // this happens at 0
    }

    /**
     * Contains the actual keys.
     */
    internal var keys: PrimitiveArray<K> = keyArraySupplier(initialSize)

    /**
     * Contains the values associated with the keys ... if this tree is used as a map.
     */
    internal var values: MutableIndexedValueCollection<V>? = valuesSupplier?.invoke(initialSize)

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
        clear()
    }

    /**
     * Clears this tree.
     */
    internal fun clear() {
        size = 0
        rootIndex = NO_INDEX
        for (i in left.indices) {
            left.setP(i, NO_INDEX)
            right.setP(i, NO_INDEX)
            parent.setP(i, NO_INDEX)
        }
    }

    /**
     * Searches for the given [key] and returns its index. If the given [key] is not found, the index of the insert position for the given [key] is
     * returned. If the tree is empty, [NO_INDEX] is returned. If [containsCheck] is set, [NO_INDEX] is also returned if the [key] is not contained in
     * this tree.
     */
    internal tailrec fun searchKey(key: K, index: Int = rootIndex, parentIndex: Int = NO_INDEX, containsCheck: Boolean = false): Int {
        if (index == NO_INDEX) {
            return if (containsCheck) NO_INDEX else parentIndex
        }
        val currentKey = keys[index]
        val compareResult = comparator.compare(key, currentKey)

        return when {
            compareResult < 0 -> searchKey(key, left.getP(index), index, containsCheck)
            compareResult > 0 -> searchKey(key, right.getP(index), index, containsCheck)
            else -> index
        }
    }

    /**
     * Checks if the given [key] is contained in the tree.
     */
    fun containsKey(key: K): Boolean = searchKey(key, containsCheck = true) >= 0

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

    /**
     * Internal function for removing the key at the given [index].
     */
    internal fun removeKeyAt(index: Int) {
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
            clear()
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

    /**
     * Checks if the key at the given [index] is a leaf, meaning both subtrees are empty.
     */
    private fun isLeaf(index: Int): Boolean = index != NO_INDEX && left.getP(index) == NO_INDEX && right.getP(index) == NO_INDEX

    /**
     * Rotates the given [index] to the left.
     */
    internal fun rotateLeft(index: Int) = internalRotate(index, left, right)

    /**
     * Rotates the given [index] to the right.
     */
    internal fun rotateRight(index: Int) = internalRotate(index, right, left)

    /**
     * Internal generic rotate function that can rotate the given [index] either to the left or the right. The direction is determined by the
     * [directionArray]. If the [directionArray] is [left], the rotation will be a left rotation. If the [directionArray] is [right], the rotation
     * will be a right rotation. The [otherArray] is required to be, as the name suggests, the other array. In case of a left rotation it has to be
     * set to [right]. In case of a right rotation it has to be set to [left].
     */
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

    /**
     * Internal function for fixing the height in bottom-up direction. It is used by [balanceUp] and [internalRotate] to adjust the stored heights.
     */
    private fun determineNewHeight(index: Int): Byte = when {
        index == NO_INDEX -> -1
        left.getP(index) == NO_INDEX && right.getP(index) == NO_INDEX -> 0
        left.getP(index) == NO_INDEX -> height.getP(right.getP(index)).inc()
        right.getP(index) == NO_INDEX -> height.getP(left.getP(index)).inc()
        else -> max(height.getP(left.getP(index)), height.getP(right.getP(index))).inc()
    }

    /**
     * This function is called by the insert and remove operations in the end for re-balancing the tree.
     */
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

    /**
     * Checks if a node is considered "locally balanced" based on the heights of the subtrees.
     */
    private fun locallyBalanced(index: Int): Boolean {
        val leftHeight = height(left.getP(index))
        val rightHeight = height(right.getP(index))
        return maxHeightDifference <= 0 || abs(leftHeight - rightHeight) <= maxHeightDifference
    }

    /**
     * "Fast" height determination. It just returns the [height] value of the root node.
     */
    fun height(): Int = height(rootIndex).toInt()

    /**
     * Internal height function for a given [index]. It returns a [Byte], because the height is stored as unsigned [Byte].
     */
    internal fun height(index: Int): Byte = if (index == NO_INDEX) -1 else height.getP(index)

    /**
     * Internal function for setting the given [value] at the given [internalIndex], if this [PrimitiveTypeBinaryTree] is
     * used as a map. The index returned by [insertKey] should be passed. The function tolerates negative values for
     * [internalIndex] by ignoring the call (no Exception). Therefore, callers don't have to perform this check
     * themselves.
     */
    internal fun insertValue(internalIndex: Int, value: V?) {
        if (internalIndex >= size) {
            throw IndexOutOfBoundsException(internalIndex)
        }
        if (internalIndex >= 0) {
            if (value == null) {
                // If you try to add null, this is considered a deletion.
                removeKeyAt(internalIndex)
            } else {
                values?.set(internalIndex, value)
            }
        }
    }

    /**
     * Retrieves the value at the given [internalIndex]. Unlike [insertValue] this function performs a boundary check
     * and throws an [IndexOutOfBoundsException].
     */
    internal fun getValueAt(internalIndex: Int): V? {
        if (internalIndex !in 0..<size) {
            throw IndexOutOfBoundsException(internalIndex)
        }
        return values?.get(internalIndex)
    }

    /**
     * Checks, if the internal [values] structure contains the given [value].
     */
    internal fun containsValue(value: V): Boolean {
        return values?.contains(value) ?: false
    }

    /**
     * Creates a mutable collection of the stored [values].
     */
    internal fun valuesAsCollection(): MutableCollection<V>? = values?.asCollection { it in 0..<size }

    override fun toString() = toStringR()

    /**
     * Creates a (slow) iterator that iterates the elements in order dictated by the [comparator].
     */
    internal fun inorderIndexIterator(): BinaryTreeInorderIndexIterator = BinaryTreeInorderIndexIterator()

    /**
     * Creates a (fast) iterator that iterates the underlying [keys] array directly with no defined order.
     */
    internal fun unorderedIndexIterator(): BinaryTreeUnorderedIndexIterator = BinaryTreeUnorderedIndexIterator()

    /**
     * Inserts the given [key] to this binary tree. Returns [NO_INDEX] if duplicates are forbidden, and it is already contained.
     */
    fun insertKey(key: K): Int {
        if (rootIndex == NO_INDEX) {
            // first insert
            rootIndex = 0
            internalAdd(key, NO_INDEX)
            return rootIndex
        }
        // search for the insert position
        val parentIndex = searchKey(key)
        assert(parentIndex != NO_INDEX) // that case should be covered by the first insert above
        return insertAtParent(parentIndex, key)
    }

    /**
     * Internal function for inserting a given [key] as subtree (leaf) of the node defined by the given [parentIndex]. The node requires an empty
     * subtree at the correct position. This requirement is not checked (only an assertion).
     */
    internal fun insertAtParent(parentIndex: Int, key: K): Int {
        val parentKey = keys[parentIndex]
        if (!allowDuplicateKeys && parentKey == key) {
            // no duplicates ... we do not use "contains" for avoiding a second search
            return NO_INDEX
        }
        // compare key for determining left or right
        val compareResult = comparator.compare(key, parentKey)
        if (compareResult < 0) {
            val leftIndex = left.getP(parentIndex)
            assert(leftIndex == NO_INDEX) // this should be the case if the search was correct
            left.setP(parentIndex, size)
        } else {
            // this covers > 0
            // If the tree allows for duplicates, those are added to the right to sustain relative order when performing an inorder iteration
            val rightIndex = right.getP(parentIndex)
            assert(rightIndex == NO_INDEX) // this should be the case if the search was correct
            right.setP(parentIndex, size)
        }
        internalAdd(key, parentIndex) // this call increases the size
        val insertIndex = size - 1
        balanceUp(insertIndex)
        return insertIndex
    }

    /**
     * Adds a key at the next free spot in the internal [keys] array. It sets the [left] and [right] subtrees to be "empty" and also sets its parent
     * reference correctly. The subtree reference of the actual [parentIndex] has to be set by the caller.
     */
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

    /**
     * Grows the size of the internal [keys] array and [values] structure (if used as a map).
     */
    private fun grow() {
        val oldCapacity: Int = keys.size
        val newCapacity: Int = keys.calculateSizeForGrow()
        val delta: Int = newCapacity - oldCapacity
        keys = keys.getResizedCopy(delta)
        left = left.getResizedCopy(delta)
        right = right.getResizedCopy(delta)
        parent = parent.getResizedCopy(delta)
        height = height.getResizedCopy(delta)
        values?.resize(delta)
        for (i in oldCapacity..<newCapacity) {
            left.setP(i, NO_INDEX)
            right.setP(i, NO_INDEX)
            parent.setP(i, NO_INDEX)
        }
    }

    /**
     * Reduces the size of the underlying structures to the logical [size] of this tree.
     */
    fun trimToSize() {
        val difference = size - keys.size
        keys = keys.getResizedCopy(difference)
        left = left.getResizedCopy(difference)
        right = right.getResizedCopy(difference)
        parent = parent.getResizedCopy(difference)
        values?.resize(difference)
    }

    /**
     * "Slow" inorder index iterator.
     */
    internal inner class BinaryTreeInorderIndexIterator : MutableIterator<Int> {
        private var currentPosition: Int
        private var iteratorChangeCount: Int
        internal var lastDeliveredIndex: Int
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

    /**
     * "Fast" unordered index iterator.
     */
    internal inner class BinaryTreeUnorderedIndexIterator : MutableIterator<Int> {
        private var iteratorChangeCount: Int = changeCount
        private var currentPosition = 0
        internal var lastDeliveredIndex: Int = NO_INDEX

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
) : PrimitiveTypeBinaryTree<Byte, Any>(
    initialSize,
    allowDuplicates,
    { size: Int -> PrimitiveByteArray(size, native) },
    comparator,
    maxHeightDifference
)

class PrimitiveShortBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Short> = DEFAULT_SHORT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Short, Any>(
    initialSize,
    allowDuplicates,
    { size: Int -> PrimitiveShortArray(size, native) },
    comparator,
    maxHeightDifference
)

class PrimitiveCharBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Char> = DEFAULT_CHAR_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Char, Any>(
    initialSize,
    allowDuplicates,
    { size: Int -> PrimitiveCharArray(size, native) },
    comparator,
    maxHeightDifference
)

class PrimitiveIntBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Int> = DEFAULT_INT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Int, Any>(initialSize, allowDuplicates, { size: Int -> PrimitiveIntArray(size, native) }, comparator, maxHeightDifference)

class PrimitiveLongBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Long> = DEFAULT_LONG_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Long, Any>(
    initialSize,
    allowDuplicates,
    { size: Int -> PrimitiveLongArray(size, native) },
    comparator,
    maxHeightDifference
)

class PrimitiveFloatBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Float> = DEFAULT_FLOAT_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Float, Any>(
    initialSize,
    allowDuplicates,
    { size: Int -> PrimitiveFloatArray(size, native) },
    comparator,
    maxHeightDifference
)

class PrimitiveDoubleBinaryTree
@JvmOverloads constructor(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    allowDuplicates: Boolean = false,
    comparator: Comparator<Double> = DEFAULT_DOUBLE_COMPARATOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
    maxHeightDifference: Byte = 1
) : PrimitiveTypeBinaryTree<Double, Any>(
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
) : PrimitiveTypeBinaryTree<UUID, Any>(initialSize, allowDuplicates, { size: Int -> UUIDArray(size, native) }, comparator, maxHeightDifference)

internal class InternalPrimitiveTypeBinaryTree<K, V>(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    comparator: Comparator<K>,
    maxHeightDifference: Byte = 1,
    keyArraySupplier: (Int) -> PrimitiveArray<K>,
    valuesSupplier: (Int) -> MutableIndexedValueCollection<V>
) : PrimitiveTypeBinaryTree<K, V>(initialSize, allowDuplicateKeys = false, keyArraySupplier, comparator, maxHeightDifference, valuesSupplier)
