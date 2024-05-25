package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable
import de.dreamcube.hornet_queen.set.HashTableSetIterator
import de.dreamcube.hornet_queen.set.PrimitiveMutableSet

abstract class HashTableBasedMap<K, V>(val hashTable: PrimitiveTypeHashTable<K, V>) : MutableMap<K, V> {

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = EntrySet()

    /**
     * The resulting [KeySet] is mutable. All methods, except [KeySet.add], will work and reflect their changes with
     * this [HashTableBasedMap].
     */
    override val keys: MutableSet<K>
        get() = KeySet(hashTable)

    override val size: Int
        get() = hashTable.size

    /**
     * You get a [MutableCollection] of the values. However, changes in this collection won't reflect in this
     * [HashTableBasedMap].
     */
    override val values: MutableCollection<V>
        get() = hashTable.valuesAsCollection() ?: mutableListOf()

    override fun clear() {
        hashTable.clear()
    }

    override fun isEmpty(): Boolean = size == 0

    override fun remove(key: K): V? {
        val internalIndex = hashTable.searchKey(key)
        if (internalIndex >= 0) {
            val result: V? = hashTable.getValueAt(internalIndex)
            hashTable.removeKeyAt(internalIndex)
            return result
        }
        return null
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach {
            put(it.key, it.value)
        }
    }

    override fun put(key: K, value: V): V? {
        var index = hashTable.searchKey(key)
        var oldValue: V? = null
        if (index < 0) {
            index = hashTable.insertKey(key)
        } else {
            oldValue = hashTable.getValueAt(index)
        }
        hashTable.insertValue(index, value)
        return oldValue
    }

    override fun get(key: K): V? {
        val index = hashTable.searchKey(key)
        if (index >= 0) {
            return hashTable.getValueAt(index)
        }
        return null
    }

    override fun containsValue(value: V): Boolean = hashTable.containsValue(value)

    override fun containsKey(key: K): Boolean = hashTable.containsKey(key)

    class KeySet<K>(private val hashTable: PrimitiveTypeHashTable<K, *>) : PrimitiveMutableSet<K> {
        override fun add(element: K): Boolean =
            throw UnsupportedOperationException("This call does not make any sense. Use the put function of the map.")

        override val size: Int
            get() = hashTable.size

        override fun clear() = hashTable.clear()

        override fun isEmpty(): Boolean = hashTable.size == 0

        override fun iterator(): MutableIterator<K> = HashTableSetIterator(hashTable)

        override fun remove(element: K): Boolean = hashTable.removeKey(element) >= 0

        override fun contains(element: K): Boolean = hashTable.containsKey(element)
    }

    inner class EntrySet : PrimitiveMutableSet<MutableMap.MutableEntry<K, V>> {

        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
            this@HashTableBasedMap[element.key] = element.value
            return true
        }

        override val size: Int
            get() = this@HashTableBasedMap.size

        override fun clear() = this@HashTableBasedMap.clear()

        override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
            val index: Int = hashTable.searchKey(element.key)
            if (index < 0) {
                return false
            }
            val value: V? = hashTable.getValueAt(index)
            return value == element.value
        }

        override fun isEmpty(): Boolean = this@HashTableBasedMap.isEmpty()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = EntrySetIterator()

        override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean =
            this@HashTableBasedMap.remove(element.key) != null

        inner class EntrySetIterator : MutableIterator<MutableMap.MutableEntry<K, V>> {
            private val actualIterator: HashTableSetIterator<K> = HashTableSetIterator(hashTable)

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
            get() = hashTable[internalIndex]

        override val value: V
            get() = hashTable.getValueAt(internalIndex)!!

        override fun setValue(newValue: V): V {
            val oldValue: V = value
            hashTable.insertValue(internalIndex, newValue)
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

    }


}