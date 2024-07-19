package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable
import de.dreamcube.hornet_queen.list.InternalPrimitiveTypeList
import de.dreamcube.hornet_queen.list.PrimitiveArrayList
import java.util.*

abstract class PrimitiveTypeValueCollection<T>(
    final override val size: Int,
    private val arraySupplier: (Int) -> PrimitiveArray<T>
) :
    PrimitiveTypeHashTable.MutableIndexedValueCollection<T> {
    val array: PrimitiveArray<T> = arraySupplier(size)

    override fun get(index: Int): T = array[index]

    override fun asCollection(): MutableCollection<T> {
        val list: PrimitiveArrayList<T> = InternalPrimitiveTypeList(arraySupplier)
        for (value in array) {
            list.add(value)
        }
        return list
    }

    override fun contains(value: T): Boolean {
        for (element: T in array) {
            if (element == value) {
                return true
            }
        }
        return false
    }

    @Suppress("kotlin:S6518") // using array[index] = value won't work here because the assignment does not return Unit
    override fun set(index: Int, value: T) = array.set(index, value)
}

class ByteValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Byte>(size, { arraySize: Int -> PrimitiveByteArray(arraySize, native) })

class ShortValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Short>(size, { arraySize: Int -> PrimitiveShortArray(arraySize, native) })

class IntValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Int>(size, { arraySize: Int -> PrimitiveIntArray(arraySize, native) })

class LongValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Long>(size, { arraySize: Int -> PrimitiveLongArray(arraySize, native) })

class FloatValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Float>(size, { arraySize: Int -> PrimitiveFloatArray(arraySize, native) })

class DoubleValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Double>(size, { arraySize: Int -> PrimitiveDoubleArray(arraySize, native) })

class UUIDValueCollection(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<UUID>(size, { arraySize: Int -> UUIDArray(arraySize, native) })
