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

package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.NO_INDEX
import de.dreamcube.hornet_queen.set.OrderedTreeSetIterator
import de.dreamcube.hornet_queen.set.PrimitiveMutableSet
import de.dreamcube.hornet_queen.set.TreeSetIterator
import de.dreamcube.hornet_queen.set.UnorderedTreeSetIterator
import de.dreamcube.hornet_queen.tree.PrimitiveTypeBinaryTree

abstract class TreeBasedMap<K, V>(val binaryTree: PrimitiveTypeBinaryTree<K, V>, private val fastIterator: Boolean = false) : MutableMap<K, V> {

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = EntrySet()

    override val keys: MutableSet<K>
        get() = KeySet(binaryTree, fastIterator)

    override val size: Int
        get() = binaryTree.size

    override val values: MutableCollection<V>
        get() = binaryTree.valuesAsCollection() ?: mutableListOf()

    override fun clear() {
        binaryTree.markAsEmpty()
    }

    @Suppress("kotlin:S6529") // we are literally implementing isEmpty() here ... following the rule would cause endless recursion
    override fun isEmpty(): Boolean = size == 0

    override fun remove(key: K): V? {
        val internalIndex = binaryTree.searchKey(key, containsCheck = true)
        if (internalIndex != NO_INDEX) {
            val result: V? = binaryTree.values?.get(internalIndex)
            binaryTree.removeKeyAt(internalIndex)
            return result
        }
        return null
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (key, value) ->
            put(key, value)
        }
    }

    override fun put(key: K, value: V): V? {
        var index = binaryTree.searchKey(key, containsCheck = false)
        var oldValue: V? = null
        // TODO: the check does not work yet on empty trees
        if (binaryTree.keys[index] == key) {
            // if we set containsCheck to false, the returned index returns either the found position (in which case the key at the index equals the
            // key we want to insert) or the index of the parent where we want to insert the new key. So the if statement is technically a contains
            // check without the liability to search again
            index = binaryTree.insertAtParent(index, key)
        } else {
            oldValue = binaryTree.getValueAt(index)
        }
        binaryTree.insertValue(index, value)
        return oldValue
    }

    override fun get(key: K): V? {
        val index = binaryTree.searchKey(key, containsCheck = true)
        if (index != NO_INDEX) {
            return binaryTree.getValueAt(index)
        }
        return null
    }

    override fun containsValue(value: V): Boolean = binaryTree.containsValue(value)

    override fun containsKey(key: K): Boolean = binaryTree.containsKey(key)

    fun trimToSize() = binaryTree.trimToSize()

    override fun toString() = entries.toString()

    class KeySet<K>(private val binaryTree: PrimitiveTypeBinaryTree<K, *>, private val fastIterator: Boolean) : PrimitiveMutableSet<K> {
        override fun add(element: K): Boolean =
            throw UnsupportedOperationException("This call does not make any sense. Use the put function of the map.")

        override val size: Int
            get() = binaryTree.size

        override fun clear() = binaryTree.markAsEmpty()

        override fun isEmpty(): Boolean = binaryTree.size == 0

        override fun iterator(): MutableIterator<K> = if (fastIterator) UnorderedTreeSetIterator(binaryTree) else OrderedTreeSetIterator(binaryTree)

        override fun remove(element: K): Boolean = binaryTree.removeKey(element) >= 0

        override fun contains(element: K): Boolean = binaryTree.containsKey(element)

        override fun toString() = asString()
    }

    inner class EntrySet : PrimitiveMutableSet<MutableMap.MutableEntry<K, V>> {

        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
            this@TreeBasedMap[element.key] = element.value
            return true
        }

        override val size: Int
            get() = this@TreeBasedMap.size

        override fun clear() = this@TreeBasedMap.clear()

        override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
            val index: Int = binaryTree.searchKey(element.key)
            if (index != NO_INDEX) {
                return false
            }
            val value: V? = binaryTree.getValueAt(index)
            return value == element.value
        }

        override fun isEmpty(): Boolean = this@TreeBasedMap.isEmpty()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = EntrySetIterator()

        override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean =
            this@TreeBasedMap.remove(element.key) != null

        override fun toString() = asString()

        inner class EntrySetIterator : MutableIterator<MutableMap.MutableEntry<K, V>> {
            private val actualIterator: TreeSetIterator<K> =
                if (fastIterator) UnorderedTreeSetIterator(binaryTree) else OrderedTreeSetIterator(binaryTree)

            override fun hasNext(): Boolean = actualIterator.hasNext()

            override fun next(): MutableMap.MutableEntry<K, V> {
                actualIterator.next()
                return Entry(actualIterator.lastDeliveredIndex)
            }

            override fun remove() = actualIterator.remove()
        }
    }

    private inner class Entry(val internalIndex: Int) : MutableMap.MutableEntry<K, V> {
        override val key: K
            get() = binaryTree.keys[internalIndex]

        override val value: V
            get() = binaryTree.getValueAt(internalIndex)!!

        override fun setValue(newValue: V): V {
            val oldValue: V = value
            binaryTree.insertValue(internalIndex, newValue)
            return oldValue
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other is MutableMap.MutableEntry<*, *>) {
                if (key != other.key) return false
                if (value != other.value) return false
                return true
            }
            return false
        }

        override fun hashCode(): Int {
            var result = key?.hashCode() ?: 0
            result = 31 * result + (value?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = "($key => $value)"
    }
}