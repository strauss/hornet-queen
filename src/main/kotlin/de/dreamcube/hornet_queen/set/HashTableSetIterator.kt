package de.dreamcube.hornet_queen.set

import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable

class HashTableSetIterator<T>(private val hashTable: PrimitiveTypeHashTable<T, *>) : MutableIterator<T> {
    private var expectedSize: Int = hashTable.size
    internal var lastDeliveredIndex = -1

    private val indexIterator: Iterator<Int> = hashTable.iterator()

    override fun hasNext(): Boolean = indexIterator.hasNext()

    override fun next(): T {
        modificationCheck()
        lastDeliveredIndex = indexIterator.next()
        return hashTable[lastDeliveredIndex]
    }

    override fun remove() {
        modificationCheck()
        if (lastDeliveredIndex < 0) {
            throw IllegalStateException()
        }
        hashTable.removeKeyAt(lastDeliveredIndex)
        lastDeliveredIndex = -1
        expectedSize -= 1
    }

    private fun modificationCheck() {
        if (expectedSize != hashTable.size) {
            throw ConcurrentModificationException()
        }
    }
}