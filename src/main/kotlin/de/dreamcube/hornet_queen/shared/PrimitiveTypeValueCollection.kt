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

package de.dreamcube.hornet_queen.shared

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import de.dreamcube.hornet_queen.list.InternalPrimitiveTypeList
import de.dreamcube.hornet_queen.list.PrimitiveArrayList
import java.util.*

abstract class PrimitiveTypeValueCollection<T>(
    final override val size: Int,
    private val arraySupplier: (Int) -> PrimitiveArray<T>
) :
    MutableIndexedValueCollection<T> {
    val array: PrimitiveArray<T> = arraySupplier(size)

    override fun get(index: Int): T = array[index]

    override fun asCollection(contained: (Int) -> Boolean): MutableCollection<T> {
        val list: PrimitiveArrayList<T> = InternalPrimitiveTypeList(arraySupplier)
        for (i: Int in 0 until array.size) {
            // we only add elements that are contained
            if (contained(i)) {
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
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Byte>(size, { arraySize: Int -> PrimitiveByteArray(arraySize, native) })

class ShortValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Short>(size, { arraySize: Int -> PrimitiveShortArray(arraySize, native) })

class CharValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Char>(size, { arraySize: Int -> PrimitiveCharArray(arraySize, native) })

class IntValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Int>(size, { arraySize: Int -> PrimitiveIntArray(arraySize, native) })

class LongValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Long>(size, { arraySize: Int -> PrimitiveLongArray(arraySize, native) })

class FloatValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Float>(size, { arraySize: Int -> PrimitiveFloatArray(arraySize, native) })

class DoubleValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<Double>(size, { arraySize: Int -> PrimitiveDoubleArray(arraySize, native) })

class UUIDValueCollection
@JvmOverloads constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveTypeValueCollection<UUID>(size, { arraySize: Int -> UUIDArray(arraySize, native) })
