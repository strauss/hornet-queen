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

import de.dreamcube.hornet_queen.BYTE_SIZE
import de.dreamcube.hornet_queen.ConfigurableConstants
import java.nio.ByteBuffer

class PrimitiveByteArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Byte>(size, BYTE_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = Int.MAX_VALUE - 8
    }

    override fun get(index: Int): Byte = internalGetByte(index)

    override fun set(index: Int, element: Byte) {
        internalSetByte(index, element)
    }

    fun getP(index: Int): Byte = internalGetByte(index)

    fun setP(index: Int, element: Byte) = internalSetByte(index, element)

    override fun getResizedCopy(difference: Int): PrimitiveByteArray {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveByteArray(newSize, native, newBuffer)
    }
}