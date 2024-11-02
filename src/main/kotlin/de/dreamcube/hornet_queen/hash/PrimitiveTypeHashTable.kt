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

package de.dreamcube.hornet_queen.hash

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.shared.FillState
import de.dreamcube.hornet_queen.shared.MutableIndexedValueCollection
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.round

/**
 * Generic implementation of a primitive hash table based on [PrimitiveArray].
 */
abstract class PrimitiveTypeHashTable<K, V> protected constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    private val loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    private val keyArraySupplier: (Int) -> PrimitiveArray<K>,
    private val valuesSupplier: ((Int, FillState) -> MutableIndexedValueCollection<V>)? = null
) {
    private var keys: PrimitiveArray<K>
    private var values: MutableIndexedValueCollection<V>?
    private var fillState: FillState
    var size: Int = 0
        private set
    private val capacity: Int
        get() = keys.size
    private val maxSize: Int
        get() = min(capacity - 1, (capacity * loadFactor).toInt())
    private var free: Int

    init {
        val desiredCapacityWithLoadFactor: Int = ceil(initialCapacity.toDouble() / loadFactor).toInt()
        val actualInitialCapacity = PrimeProvider.getNextRelevantPrime(desiredCapacityWithLoadFactor)
        keys = keyArraySupplier(actualInitialCapacity)
        fillState = FillState(actualInitialCapacity)
        values = valuesSupplier?.invoke(actualInitialCapacity, fillState)
        free = capacity
    }

    /**
     * Calls the internal hash function and ensures positive integers, even for [Int.MIN_VALUE].
     */
    fun hash(value: K): Int = value.hashCode() and 0x7fffffff

    /**
     * Probe value for iterating through the virtual collision list.
     */
    private fun getProbe(hashValue: Int): Int = 1 + (hashValue % (capacity - 2))

    /**
     * Searches for the given [key] and returns its index. If the given [key] is not found, -1 is returned.
     */
    internal fun searchKey(key: K): Int {
        val hash: Int = hash(key)
        val index = hash % capacity

        if (fillState.isFree(index)) {
            // not found
            return -1
        }
        if (fillState.isFull(index) && keys.equalsAt(index, key)) {
            // found
            return index
        }

        // collision
        return searchKeyAfterCollision(key, index, hash)
    }

    /**
     * Searches the given [value] in the virtual collision list.
     */
    private fun searchKeyAfterCollision(value: K, startIndex: Int, hashValue: Int): Int {
        val probe: Int = getProbe(hashValue)
        var currentIndex: Int = startIndex - probe
        if (currentIndex < 0) {
            currentIndex += capacity
        }
        while (currentIndex != startIndex) {
            if (fillState.isFree(currentIndex)) {
                // reached first free index in collision "list" -> not found
                return -1
            }
            // removed cells are skipped and values have to match
            if (!fillState.isRemoved(currentIndex) && keys.equalsAt(currentIndex, value)) {
                // found
                return currentIndex
            }
            currentIndex -= probe
            if (currentIndex < 0) {
                currentIndex += capacity
            }
        }
        return -1 // searched the whole collision "list" -> not found
    }

    /**
     * Checks if the given [key] is contained in the hash table.
     */
    fun containsKey(key: K): Boolean = searchKey(key) >= 0

    /**
     * Removes the given [key] and returns the index it was removed from.
     */
    fun removeKey(key: K): Int {
        val index = searchKey(key)
        if (index >= 0) {
            removeKeyAt(index)
        }
        return index
    }

    /**
     * Marks the given [internalIndex] as removed.
     */
    internal fun removeKeyAt(internalIndex: Int) {
        size -= 1

        // automatic compaction would come here, but we leave it out for the moment

        fillState.setRemoved(internalIndex)

        // we could remove the value from the array (set zero/neutral/whatever), but this is not required because the fill state already marks the
        // position as empty/removed
    }

    /**
     * Inserts the given [key] into this hash table. Returns -1 if it is already contained.
     */
    fun insertKey(key: K): Int {
        // Perform rehash if required
        if (size >= maxSize || free == 0) {
            val newCapacity = if (size > maxSize) PrimeProvider.getNextRelevantPrime(capacity shl 1) else capacity
            rehash(newCapacity)
        }
        val hashValue = hash(key)
        val index = hashValue % capacity

        if (fillState.isFree(index)) {
            insertKeyAt(index, key)
            postInsert(false)
            return index
        }

        if (fillState.isFull(index) && keys.equalsAt(index, key)) {
            // already there
            return -index - 1
        }

        // Collision (full or removed)
        return insertKeyAfterCollision(key, index, hashValue)
    }

    /**
     * Internal insert function in case of collision.
     */
    @Suppress("kotlin:S3776") // We will ignore cognitive complexity for now ... let's pretend it happens for performance reasons :-)
    private fun insertKeyAfterCollision(value: K, startIndex: Int, hashValue: Int): Int {
        val probe: Int = getProbe(hashValue)
        // if the found index is a "removed" index, we want to reuse it. We are only allowed to do so, if the next index is free
        var firstRemovedIndex: Int = if (fillState.isRemoved(startIndex)) startIndex else -1

        var currentIndex: Int = startIndex - probe
        if (currentIndex < 0) {
            currentIndex += capacity
        }
        while (currentIndex != startIndex) {

            if (fillState.isFree(currentIndex)) {
                // if we stumbled upon a removed index, we use it, otherwise we take the found free slot
                val reuseRemovedIndex: Boolean = firstRemovedIndex != -1
                val targetIndex: Int = if (reuseRemovedIndex) firstRemovedIndex else currentIndex
                insertKeyAt(targetIndex, value)
                postInsert(reuseRemovedIndex)
                return targetIndex
            }

            if (fillState.isFull(currentIndex) && keys.equalsAt(currentIndex, value)) {
                // already there
                return -currentIndex - 1
            }

            // detect first removed if it occurs before first free
            if (firstRemovedIndex == -1 && fillState.isRemoved(currentIndex)) {
                firstRemovedIndex = currentIndex
            }
            currentIndex -= probe
            if (currentIndex < 0) {
                currentIndex += capacity
            }
        }

        // edge case: we have a first removed index but no more free indexes
        if (firstRemovedIndex == -1) {
            insertKeyAt(firstRemovedIndex, value)
            postInsert(true)
            return firstRemovedIndex
        }
        throw IllegalStateException("No free or removed slots available. Hash table is full?!!")
    }

    /**
     * Writes the given [key] to the given [internalIndex] and marks the [internalIndex] as full.
     */
    private fun insertKeyAt(internalIndex: Int, key: K) {
        keys[internalIndex] = key
        fillState.setFull(internalIndex)
    }

    /**
     * Manages the internal state after an insertion operation.
     */
    private fun postInsert(reuseRemovedIndex: Boolean) {
        if (!reuseRemovedIndex) {
            free -= 1
        }
        size += 1
    }

    /**
     * Internal function for setting the given [value] at the given [internalIndex], if this [PrimitiveTypeHashTable] is
     * used as a set. The index returned by [insertKey] should be passed. The function tolerates negative values for
     * [internalIndex] by ignoring the call (no Exception). Therefore callers don't have to perform this check
     * themselves.
     */
    internal fun insertValue(internalIndex: Int, value: V?) {
        if (internalIndex >= capacity) {
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
        if (internalIndex < 0 || internalIndex >= capacity) {
            throw IndexOutOfBoundsException(internalIndex)
        }
        return values?.get(internalIndex)
    }

    internal fun containsValue(value: V): Boolean {
        return values?.contains(value) ?: false
    }

    internal fun valuesAsCollection(): MutableCollection<V>? = values?.asCollection()

    /**
     * Resizes this hash table to the [newCapacity] and rehashes all values. The default value for capacity is the current capacity. This results
     * in a rehash without a resize. Deleted cells are freed and new space appears.
     */
    internal fun rehash(newCapacity: Int = capacity) {
        val oldCapacity = capacity
        val oldHashTable = keys
        val oldValues = values
        val oldFillState = fillState
        free = newCapacity
        size = 0

        keys = keyArraySupplier(newCapacity)
        fillState = FillState(newCapacity)
        values = valuesSupplier?.invoke(newCapacity, fillState)
        for (i in 0..<oldCapacity) {
            if (oldFillState.isFull(i)) {
                val key: K = oldHashTable[i]
                val index: Int = insertKey(key)
                if (oldValues != null) {
                    insertValue(index, oldValues[i])
                }
            }
        }
    }

    /**
     * Determines the maximum required capacity and shrinks the structure according to the configured fill state. The resulting capacity is in general
     * a bit larger because the "allowed" capacities are limited to certain prime numbers (see [PrimeProvider]).
     */
    internal fun shrinkToLoadFactor() {
        val requiredCapacity = round(size / loadFactor).toInt()
        val newCapacity = PrimeProvider.getNextRelevantPrime(requiredCapacity)
        rehash(newCapacity)
    }

    fun clear() {
        free = capacity
        size = 0
        for (i in 0..<capacity) {
            // clear field ... or not
            fillState.setFree(i)
        }
    }

    internal operator fun get(internalIndex: Int): K {
        if (fillState.isFull(internalIndex)) {
            return keys[internalIndex]
        }
        throw NoSuchElementException("No element at internal index $internalIndex!")
    }

    internal fun iterator(): Iterator<Int> = HashTableIndexIterator()

    /**
     * Iterator for returning all occupied indexes of this [PrimitiveTypeHashTable].
     */
    internal inner class HashTableIndexIterator : Iterator<Int> {
        private var currentInternalIndex = 0

        init {
            seek()
        }

        private fun seek() {
            while (currentInternalIndex < capacity && !fillState.isFull(currentInternalIndex)) {
                currentInternalIndex += 1
            }
        }

        override fun hasNext(): Boolean = currentInternalIndex < capacity

        override fun next(): Int {
            val result: Int = currentInternalIndex
            currentInternalIndex += 1
            seek()
            return result
        }

    }

}

class PrimitiveByteHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Byte, Any>(initialCapacity, loadFactor, { size: Int -> PrimitiveByteArray(size, native) })

class PrimitiveShortHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Short, Any>(initialCapacity, loadFactor, { size: Int -> PrimitiveShortArray(size, native) })

class PrimitiveCharHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Char, Any>(initialCapacity, loadFactor, { size: Int -> PrimitiveCharArray(size, native) })

class PrimitiveIntHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Int, Any>(initialCapacity, loadFactor, { size: Int -> PrimitiveIntArray(size, native) })

class PrimitiveLongHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Long, Any>(initialCapacity, loadFactor, { size: Int -> PrimitiveLongArray(size, native) })

class PrimitiveFloatHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Float, Any>(initialCapacity, loadFactor, { size: Int -> PrimitiveFloatArray(size, native) })

class PrimitiveDoubleHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<Double, Any>(initialCapacity, loadFactor,
    { size: Int -> PrimitiveDoubleArray(size, native) })

class UUIDHashTable @JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : PrimitiveTypeHashTable<UUID, Any>(initialCapacity, loadFactor, { size: Int -> UUIDArray(size, native) })

internal class InternalPrimitiveTypeHashTable<K, V>(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    keyArraySupplier: (Int) -> PrimitiveArray<K>,
    valuesSupplier: ((Int, FillState) -> MutableIndexedValueCollection<V>)
) : PrimitiveTypeHashTable<K, V>(initialCapacity, loadFactor, keyArraySupplier, valuesSupplier)
