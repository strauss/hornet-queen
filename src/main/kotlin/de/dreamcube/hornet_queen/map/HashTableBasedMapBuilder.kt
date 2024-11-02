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

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.hash.InternalPrimitiveTypeHashTable
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder.HashTableBasedMapBuilderWithKey
import de.dreamcube.hornet_queen.shared.*
import java.util.*

/**
 * This builder object assists in creating a new [HashTableBasedMap]. Unfortunately it is not feasible to create classes
 * for every combination of types. It is also not possible to simply create an object like a regular [HashMap] solely by
 * the given types, because the underlying data structures all have dedicated types (derived from [PrimitiveArray]).
 * The builder object is stateless and provides methods for configuring the key type. The result of a configuring
 * function is an object of [HashTableBasedMapBuilderWithKey] for configuring the value type.
 */
object HashTableBasedMapBuilder {

    /**
     * Configures the resulting [HashTableBasedMap] to use [Byte] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useByteKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Byte> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveByteArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Short] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useShortKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Short> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveShortArray(size, native) }

    /**
     * Configurs the resulting [HashTableBasedMap] to use [Char] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useCharKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Char> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveCharArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Int] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useIntKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Int> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveIntArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Long] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useLongKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Long> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveLongArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Float] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useFloatKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Float> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveFloatArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Double] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useDoubleKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Double> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveDoubleArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [UUID] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useUUIDKey(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<UUID> =
        HashTableBasedMapBuilderWithKey { size: Int -> UUIDArray(size, native) }

    /**
     * This is an intermediate builder class with the key type [K] preconfigured. It provides methods for configuring
     * the value type. After configuring the value type, a final object of type
     * [HashTableBasedMapBuilderWithKeyAndValue] is returned that can be used to finally create the map.
     */
    class HashTableBasedMapBuilderWithKey<K> internal constructor(private val keyArraySupplier: (Int) -> PrimitiveArray<K>) {

        /**
         * Configures the resulting [HashTableBasedMap] to use [Byte] as value type.
         */
        @JvmOverloads
        fun useByteValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Byte> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                ByteValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Short] as value type.
         */
        @JvmOverloads
        fun useShortValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Short> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                ShortValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Char] as value type.
         */
        @JvmOverloads
        fun useCharValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Char> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                CharValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Int] as value type.
         */
        @JvmOverloads
        fun useIntValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Int> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                IntValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Long] as value type.
         */
        @JvmOverloads
        fun useLongValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Long> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                LongValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Float] as value type.
         */
        @JvmOverloads
        fun useFloatValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Float> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                FloatValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Double] as value type.
         */
        @JvmOverloads
        fun useDoubleValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Double> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                DoubleValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [UUID] as value type.
         */
        @JvmOverloads
        fun useUUIDValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, UUID> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                UUIDValueCollection(size, fillState, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [V] as value type. The internal value structure will be a
         * normal object array.
         */
        fun <V> useArbitraryTypeValue(): HashTableBasedMapBuilderWithKeyAndValue<K, V> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int, fillState: FillState ->
                ObjectTypeValueCollection(size, fillState)
            }
    }

    /**
     * Final builder class for creating the map.
     */
    class HashTableBasedMapBuilderWithKeyAndValue<K, V>
    internal constructor(
        private val keyArraySupplier: (Int) -> PrimitiveArray<K>,
        private val valuesSupplier: (Int, FillState) -> MutableIndexedValueCollection<V>
    ) {

        /**
         * This function will create the map. The given [initialCapacity] and [loadFactor] can be used to tweak the
         * map's performance (space or time).
         */
        @JvmOverloads
        fun create(
            initialCapacity: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
            loadFactor: Double = ConfigurableConstants.DEFAULT_LOAD_FACTOR
        ): HashTableBasedMap<K, V> =
            InternalHashTableBasedMap(initialCapacity, loadFactor, keyArraySupplier, valuesSupplier)
    }

    /**
     * This class is an internal class for allowing the abstract [HashTableBasedMap] to be instantiated. This also
     * forces developers to use the builder object [HashTableBasedMapBuilder] instead of using the class directly.
     */
    internal class InternalHashTableBasedMap<K, V>(
        initialCapacity: Int,
        loadFactor: Double,
        keyArraySupplier: (Int) -> PrimitiveArray<K>,
        valuesSupplier: (Int, FillState) -> MutableIndexedValueCollection<V>
    ) : HashTableBasedMap<K, V>(
        InternalPrimitiveTypeHashTable(initialCapacity, loadFactor, keyArraySupplier, valuesSupplier)
    )

}
