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

package de.dreamcube.hornet_queen.array

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.LONG_SIZE
import de.dreamcube.hornet_queen.UUID_SHIFT
import de.dreamcube.hornet_queen.UUID_SIZE
import java.nio.ByteBuffer
import java.util.*

class UUIDArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<UUID>(size, UUID_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / UUID_SIZE
    }

    override operator fun get(index: Int): UUID {
        val lowerIndex = index shl UUID_SHIFT
        val upperIndex = lowerIndex + LONG_SIZE
        val lower: Long = internalGetLongDirectly(lowerIndex)
        val upper: Long = internalGetLongDirectly(upperIndex)
        return UUID(upper, lower)
    }

    override operator fun set(index: Int, element: UUID) {
        val lowerIndex = index shl UUID_SHIFT
        val upperIndex = lowerIndex + LONG_SIZE
        internalSetLongDirectly(lowerIndex, element.leastSignificantBits)
        internalSetLongDirectly(upperIndex, element.mostSignificantBits)
    }

    override fun equalsAt(index: Int, otherElement: UUID): Boolean {
        val lowerIndex = index shl UUID_SHIFT
        val lower = internalGetLongDirectly(lowerIndex)
        if (lower == otherElement.leastSignificantBits) {
            val upperIndex = lowerIndex + LONG_SIZE
            val upper = internalGetLongDirectly(upperIndex)
            return upper == otherElement.mostSignificantBits
        }
        return false
    }

    override fun getResizedCopy(difference: Int): UUIDArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return UUIDArray(newSize, native, newBuffer)
    }

}