package de.snoopy1alpha.primitive_collections.set

import de.snoopy1alpha.primitive_collections.DEFAULT_INITIAL_SIZE
import de.snoopy1alpha.primitive_collections.DEFAULT_LOAD_FACTOR
import de.snoopy1alpha.primitive_collections.DEFAULT_NATIVE
import de.snoopy1alpha.primitive_collections.hash.*
import java.util.*

abstract class HashTableBasedSet<T>(private val hashTable: PrimitiveTypeHashTable<T, Any>) : PrimitiveMutableSet<T> {

    override fun add(element: T): Boolean = hashTable.insertKey(element) >= 0

    override val size: Int
        get() = hashTable.size

    override fun clear() {
        hashTable.clear()
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> = HashTableSetIterator(hashTable)

    override fun remove(element: T): Boolean = hashTable.removeKey(element) >= 0

    override fun contains(element: T): Boolean = hashTable.containsKey(element)

}

class PrimitiveByteSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<Byte>(PrimitiveByteHashTable(initialCapacity, loadFactor, native))

class PrimitiveShortSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<Short>(PrimitiveShortHashTable(initialCapacity, loadFactor, native))

class PrimitiveIntSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<Int>(PrimitiveIntHashTable(initialCapacity, loadFactor, native))

class PrimitiveLongSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<Long>(PrimitiveLongHashTable(initialCapacity, loadFactor, native))

class PrimitiveFloatSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<Float>(PrimitiveFloatHashTable(initialCapacity, loadFactor, native))

class PrimitiveDoubleSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<Double>(PrimitiveDoubleHashTable(initialCapacity, loadFactor, native))

class UUIDSet(
    initialCapacity: Int = DEFAULT_INITIAL_SIZE,
    loadFactor: Double = DEFAULT_LOAD_FACTOR,
    native: Boolean = DEFAULT_NATIVE
) : HashTableBasedSet<UUID>(UUIDHashTable(initialCapacity, loadFactor, native))

