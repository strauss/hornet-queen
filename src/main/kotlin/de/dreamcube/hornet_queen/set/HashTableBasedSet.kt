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

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.hash.*
import java.util.*

abstract class HashTableBasedSet<T>(private val hashTable: PrimitiveTypeHashTable<T, Any>) : PrimitiveMutableSet<T> {

    override fun add(element: T): Boolean = hashTable.insertKey(element) >= 0

    override val size: Int
        get() = hashTable.size

    override fun clear() {
        hashTable.clear()
    }

    @Suppress("kotlin:S6529") // we are literally implementing isEmpty() here ... following the rule would cause endless recursion
    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> = HashTableSetIterator(hashTable)

    override fun remove(element: T): Boolean = hashTable.removeKey(element) >= 0

    override fun contains(element: T): Boolean = hashTable.containsKey(element)

    /**
     * This function can be called if too many cells have been marked as deleted. It frees cells without increasing the memory requirements.
     */
    fun manualRehash() = hashTable.rehash()

    /**
     * This function can be called if free space is required and this structure should be shrunk to the actual required space. The best situation for
     * calling this function is when you know that you will not add any new elements.
     */
    fun shrinkToLoadFactor() = hashTable.shrinkToLoadFactor()
}

class PrimitiveByteSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Byte>(PrimitiveByteHashTable(initialCapacity, loadFactor, native))

class PrimitiveShortSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Short>(PrimitiveShortHashTable(initialCapacity, loadFactor, native))

class PrimitiveCharSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Char>(PrimitiveCharHashTable(initialCapacity, loadFactor, native))

class PrimitiveIntSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Int>(PrimitiveIntHashTable(initialCapacity, loadFactor, native))

class PrimitiveLongSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Long>(PrimitiveLongHashTable(initialCapacity, loadFactor, native))

class PrimitiveFloatSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Float>(PrimitiveFloatHashTable(initialCapacity, loadFactor, native))

class PrimitiveDoubleSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<Double>(PrimitiveDoubleHashTable(initialCapacity, loadFactor, native))

class UUIDSet
@JvmOverloads constructor(
    initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) : HashTableBasedSet<UUID>(UUIDHashTable(initialCapacity, loadFactor, native))
