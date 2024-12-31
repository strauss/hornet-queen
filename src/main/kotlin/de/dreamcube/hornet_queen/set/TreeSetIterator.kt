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

import de.dreamcube.hornet_queen.tree.PrimitiveTypeBinaryTree

/**
 * Convenience interface for allowing accessing the last delivered index. This is required for the entry set iterator.
 */
interface TreeSetIterator<T> : MutableIterator<T> {
    val lastDeliveredIndex: Int
}

/**
 * Adapts the inorder index iterator provided by the [PrimitiveTypeBinaryTree] to an iterator on the keys of the tree.
 */
class OrderedTreeSetIterator<K>(private val binaryTree: PrimitiveTypeBinaryTree<K, *>) : TreeSetIterator<K> {
    private val internalIterator: PrimitiveTypeBinaryTree<K, *>.BinaryTreeInorderIndexIterator = binaryTree.inorderIndexIterator()

    override val lastDeliveredIndex
        get() = internalIterator.lastDeliveredIndex

    override fun hasNext(): Boolean = internalIterator.hasNext()

    override fun next(): K = binaryTree.keys[internalIterator.next()]

    override fun remove() = internalIterator.remove()
}

/**
 * Adapts the unordered index iterator provided by the [PrimitiveTypeBinaryTree] to an iterator on the keys of the tree.
 */
class UnorderedTreeSetIterator<K>(private val binaryTree: PrimitiveTypeBinaryTree<K, *>) : TreeSetIterator<K> {
    private val internalIterator: PrimitiveTypeBinaryTree<K, *>.BinaryTreeUnorderedIndexIterator = binaryTree.unorderedIndexIterator()

    override val lastDeliveredIndex
        get() = internalIterator.lastDeliveredIndex

    override fun hasNext(): Boolean = internalIterator.hasNext()

    override fun next(): K = binaryTree.keys[internalIterator.next()]

    override fun remove() = internalIterator.remove()
}
