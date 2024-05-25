package de.snoopy1alpha.primitive_collections.map

import de.snoopy1alpha.primitive_collections.DEFAULT_NATIVE
import de.snoopy1alpha.primitive_collections.array.*
import de.snoopy1alpha.primitive_collections.hash.PrimitiveTypeHashTable
import de.snoopy1alpha.primitive_collections.list.InternalPrimitiveTypeList
import de.snoopy1alpha.primitive_collections.list.PrimitiveArrayList
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

    override fun set(index: Int, value: T) = array.set(index, value)
}

class ByteValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Byte>(size, { arraySize: Int -> PrimitiveByteArray(arraySize, native) })

class ShortValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Short>(size, { arraySize: Int -> PrimitiveShortArray(arraySize, native) })

class IntValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Int>(size, { arraySize: Int -> PrimitiveIntArray(arraySize, native) })

class LongValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Long>(size, { arraySize: Int -> PrimitiveLongArray(arraySize, native) })

class FloatValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Float>(size, { arraySize: Int -> PrimitiveFloatArray(arraySize, native) })

class DoubleValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Double>(size, { arraySize: Int -> PrimitiveDoubleArray(arraySize, native) })

class UUIDValueCollection(size: Int, native: Boolean = DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<UUID>(size, { arraySize: Int -> UUIDArray(arraySize, native) })
