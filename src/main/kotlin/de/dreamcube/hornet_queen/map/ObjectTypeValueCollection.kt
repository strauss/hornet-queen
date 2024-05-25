package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable

class ObjectTypeValueCollection<T>(override val size: Int) : PrimitiveTypeHashTable.MutableIndexedValueCollection<T> {
    val array: Array<Any?> = Array(size) { null }

    override fun get(index: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return array[index] as T
    }

    override fun asCollection(): MutableCollection<T> {
        val result: MutableList<T> = mutableListOf()
        for (element: Any? in array) {
            @Suppress("UNCHECKED_CAST")
            result.add(element as T)
        }
        return result
    }

    override fun contains(value: T): Boolean {
        for (element: Any? in array) {
            if (element == value) {
                return true
            }
        }
        return false
    }

    override fun set(index: Int, value: T) {
        array[index] = value
    }
}