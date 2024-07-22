package de.dreamcube.hornet_queen.shared

/**
 * This interface is meant to wrap an array-like structure for values if this [PrimitiveTypeHashTable] is used as
 * foundation for a map implementation. This generic approach allows for primitive values in [PrimitiveArray]s and
 * also for object type values in regular Object arrays. The functions [get], [set], and [contains] are as you would
 * expect them to be in a regular array or collection.
 */
interface MutableIndexedValueCollection<V> {
    val size: Int
    val fillState: FillState
    operator fun get(index: Int): V?
    operator fun set(index: Int, value: V)
    fun contains(value: V): Boolean

    /**
     * Copies all values into a [MutableCollection]. Changes to this collection will not affect this
     * [PrimitiveTypeHashTable].
     */
    fun asCollection(): MutableCollection<V>
}