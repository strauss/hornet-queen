package de.dreamcube.hornet_queen.shared

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.list.InternalPrimitiveTypeList
import de.dreamcube.hornet_queen.list.PrimitiveArrayList
import java.util.*

abstract class PrimitiveTypeValueCollection<T>(
    final override val size: Int,
    override val fillState: FillState,
    private val arraySupplier: (Int) -> PrimitiveArray<T>
) :
    MutableIndexedValueCollection<T> {
    val array: PrimitiveArray<T> = arraySupplier(size)

    override fun get(index: Int): T = array[index]

    override fun asCollection(): MutableCollection<T> {
        val list: PrimitiveArrayList<T> = InternalPrimitiveTypeList(arraySupplier)
        for (i: Int in 0 until array.size) {
            // we only add elements that are filled
            if (fillState.isFull(i)) {
                list.add(array[i])
            }
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

class ByteValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Byte>(size, fillState, { arraySize: Int -> PrimitiveByteArray(arraySize, native) })

class ShortValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Short>(size, fillState, { arraySize: Int -> PrimitiveShortArray(arraySize, native) })

class IntValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Int>(size, fillState, { arraySize: Int -> PrimitiveIntArray(arraySize, native) })

class LongValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Long>(size, fillState, { arraySize: Int -> PrimitiveLongArray(arraySize, native) })

class FloatValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Float>(size, fillState, { arraySize: Int -> PrimitiveFloatArray(arraySize, native) })

class DoubleValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Double>(size, fillState, { arraySize: Int -> PrimitiveDoubleArray(arraySize, native) })

class UUIDValueCollection
@JvmOverloads constructor(size: Int, fillState: FillState, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<UUID>(size, fillState, { arraySize: Int -> UUIDArray(arraySize, native) })
