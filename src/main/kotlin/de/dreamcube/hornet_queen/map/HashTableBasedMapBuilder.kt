package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.DEFAULT_INITIAL_SIZE
import de.dreamcube.hornet_queen.DEFAULT_LOAD_FACTOR
import de.dreamcube.hornet_queen.DEFAULT_NATIVE
import de.dreamcube.hornet_queen.hash.InternalPrimitiveTypeHashTable
import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable.MutableIndexedValueCollection
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder.HashTableBasedMapBuilderWithKey
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
    fun useByteKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Byte> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveByteArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Short] as key type.
     */
    fun useShortKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Short> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveShortArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Int] as key type.
     */
    fun useIntKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Int> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveIntArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Long] as key type.
     */
    fun useLongKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Long> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveLongArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Float] as key type.
     */
    fun useFloatKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Float> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveFloatArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [Double] as key type.
     */
    fun useDoubleKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<Double> =
        HashTableBasedMapBuilderWithKey { size: Int -> PrimitiveDoubleArray(size, native) }

    /**
     * Configures the resulting [HashTableBasedMap] to use [UUID] as key type.
     */
    fun useUUIDKey(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKey<UUID> =
        HashTableBasedMapBuilderWithKey { size: Int -> UUIDArray(size, native) }

    /**
     * This is an intermediate builder class with the key type [K] preconfigured. It provides methods for configuring
     * the value type. After configuring the value type, a final object of type
     * [HashTableBasedMapBuilderWithKeyAndValue] is returned that can be used to finally create the map.
     */
    class HashTableBasedMapBuilderWithKey<K>(private val keyArraySupplier: (Int) -> PrimitiveArray<K>) {

        /**
         * Configures the resulting [HashTableBasedMap] to use [Byte] as value type.
         */
        fun useByteValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Byte> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                ByteValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Short] as value type.
         */
        fun useShortValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Short> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                ShortValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Int] as value type.
         */
        fun useIntValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Int> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                IntValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Long] as value type.
         */
        fun useLongValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Long> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                LongValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Float] as value type.
         */
        fun useFloatValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Float> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                FloatValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [Double] as value type.
         */
        fun useDoubleValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, Double> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                DoubleValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [UUID] as value type.
         */
        fun useUUIDValue(native: Boolean = DEFAULT_NATIVE): HashTableBasedMapBuilderWithKeyAndValue<K, UUID> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                UUIDValueCollection(size, native)
            }

        /**
         * Configures the resulting [HashTableBasedMap] to use [V] as value type. The internal value structure will be a
         * normal object array.
         */
        fun <V> useArbitraryTypeValue(): HashTableBasedMapBuilderWithKeyAndValue<K, V> =
            HashTableBasedMapBuilderWithKeyAndValue(keyArraySupplier) { size: Int ->
                ObjectTypeValueCollection(size)
            }
    }

    /**
     * Final builder class for creating the map.
     */
    class HashTableBasedMapBuilderWithKeyAndValue<K, V>(
        private val keyArraySupplier: (Int) -> PrimitiveArray<K>,
        private val valuesSupplier: (Int) -> MutableIndexedValueCollection<V>
    ) {

        /**
         * This function will create the map. The given [initialCapacity] and [loadFactor] can be used to tweak the
         * map's performance (space or time).
         */
        fun create(
            initialCapacity: Int = DEFAULT_INITIAL_SIZE,
            loadFactor: Double = DEFAULT_LOAD_FACTOR
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
        valuesSupplier: (Int) -> MutableIndexedValueCollection<V>
    ) : HashTableBasedMap<K, V>(
        InternalPrimitiveTypeHashTable(initialCapacity, loadFactor, keyArraySupplier, valuesSupplier)
    )

}
