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

import de.dreamcube.hornet_queen.*
import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.map.TreeBasedMapBuilder.TreeBasedMapBuilderWithKeyAndComparator
import de.dreamcube.hornet_queen.shared.*
import de.dreamcube.hornet_queen.tree.InternalPrimitiveTypeBinaryTree
import java.util.*

/**
 * This builder object assists in creating a new [TreeBasedMap]. Unfortunately it is not feasible to create classes for every combination of types. It
 * is also not possible to simply create an object like a regular [TreeMap] solely by the given types, because the underlying data structures all have
 * dedicated types (derived from [PrimitiveArray]). The builder object is stateless and provides functions for configuring the key type. The result of
 * a configuring function is an object of [TreeBasedMapBuilderWithKeyAndComparator] for configuring the value type.
 */
object TreeBasedMapBuilder {

    /**
     * Configures the resulting [TreeBasedMap] to use [Byte] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useByteKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Byte> = DEFAULT_BYTE_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<Byte> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveByteArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [Short] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useShortKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Short> = DEFAULT_SHORT_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<Short> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveShortArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [Char] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useCharKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Char>
    ): TreeBasedMapBuilderWithKeyAndComparator<Char> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveCharArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [Int] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useIntKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Int> = DEFAULT_INT_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<Int> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveIntArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [Long] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useLongKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Long> = DEFAULT_LONG_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<Long> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveLongArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [Float] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useFloatKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Float> = DEFAULT_FLOAT_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<Float> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveFloatArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [Double] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useDoubleKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<Double> = DEFAULT_DOUBLE_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<Double> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> PrimitiveDoubleArray(size, native) }

    /**
     * Configures the resulting [TreeBasedMap] to use [UUID] as key type.
     */
    @JvmOverloads
    @JvmStatic
    fun useUUIDKey(
        native: Boolean = ConfigurableConstants.DEFAULT_NATIVE,
        comparator: Comparator<UUID> = DEFAULT_UUID_COMPARATOR
    ): TreeBasedMapBuilderWithKeyAndComparator<UUID> =
        TreeBasedMapBuilderWithKeyAndComparator(comparator) { size: Int -> UUIDArray(size, native) }

    /**
     * This is an intermediate builder class with the key type [K] preconfigured. It provides methods for configuring the value type. After
     * configuring the value type, a final object of type [TreeBasedMapBuilderWithKeyComparatorAndValue] is returned that can be used to finally
     * create the map.
     */
    class TreeBasedMapBuilderWithKeyAndComparator<K> internal constructor(
        private val comparator: Comparator<K>,
        private val keyArraySupplier: (Int) -> PrimitiveArray<K>
    ) {

        /**
         * Configures the resulting [TreeBasedMap] to use [Byte] as value type.
         */
        @JvmOverloads
        fun useByteValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Byte> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> ByteValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [Short] as value type.
         */
        @JvmOverloads
        fun useShortValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Short> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> ShortValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [Char] as value type.
         */
        @JvmOverloads
        fun useCharValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Char> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> CharValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [Int] as value type.
         */
        @JvmOverloads
        fun useIntValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Int> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> IntValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [Long] as value type.
         */
        @JvmOverloads
        fun useLongValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Long> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> LongValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [Float] as value type.
         */
        @JvmOverloads
        fun useFloatValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Float> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> FloatValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [Double] as value type.
         */
        @JvmOverloads
        fun useDoubleValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, Double> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> DoubleValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [UUID] as value type.
         */
        @JvmOverloads
        fun useUUIDValue(native: Boolean = ConfigurableConstants.DEFAULT_NATIVE): TreeBasedMapBuilderWithKeyComparatorAndValue<K, UUID> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> UUIDValueCollection(size, native) }

        /**
         * Configures the resulting [TreeBasedMap] to use [V] as value type. The underlying value structure will be a generic [Array] of [Any]
         * containing objects instead of primitive types.
         */
        fun <V> useArbitraryTypeValue(): TreeBasedMapBuilderWithKeyComparatorAndValue<K, V> =
            TreeBasedMapBuilderWithKeyComparatorAndValue(keyArraySupplier, comparator) { size: Int -> ObjectTypeValueCollection(size) }
    }

    /**
     * Final builder class for creating the map.
     */
    class TreeBasedMapBuilderWithKeyComparatorAndValue<K, V>
    internal constructor(
        private val keyArraySupplier: (Int) -> PrimitiveArray<K>,
        private val comparator: Comparator<K>,
        private val valuesSupplier: (Int) -> MutableIndexedValueCollection<V>
    ) {

        /**
         * This function will create the map. The given [initialSize] can be used to predetermine the size of the tree. If [fastIterator] is set to
         * true, all iterators will be faster but not in order. The value [maxHeightDifference] is used for defining when a (sub) tree is considered
         * "balanced". The default value is "1" (usual AVL tree). Higher values result in less rotation operations but increase search time. The value
         * 0 is not allowed (1 will be used instead). The upper limit is 5. If a completely unbalanced tree is desired, use a value below 0. Please
         * note, that there is a height limit of 127 (unsigned bytes are used for storing the height in each node). If you create an unbalanced tree
         * with extreme heights, bad things will happen :-)
         */
        @JvmOverloads
        fun create(
            initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
            fastIterator: Boolean = false,
            maxHeightDifference: Byte = 1
        ): TreeBasedMap<K, V> = InternalTreeBasedMap(initialSize, comparator, maxHeightDifference, fastIterator, keyArraySupplier, valuesSupplier)
    }

    /**
     * This class is an internal class for allowing the abstract [TreeBasedMap] to be instantiated. This also
     * forces developers to use the builder object [TreeBasedMapBuilder] instead of using the class directly.
     */
    internal class InternalTreeBasedMap<K, V>(
        initialSize: Int,
        comparator: Comparator<K>,
        maxHeightDifference: Byte,
        fastIterator: Boolean,
        keyArraySupplier: (Int) -> PrimitiveArray<K>,
        valueSupplier: (Int) -> MutableIndexedValueCollection<V>
    ) : TreeBasedMap<K, V>(
        InternalPrimitiveTypeBinaryTree(initialSize, comparator, maxHeightDifference, keyArraySupplier, valueSupplier),
        fastIterator
    )
}