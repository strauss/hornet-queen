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

import de.dreamcube.hornet_queen.CHAR_SIZE
import de.dreamcube.hornet_queen.ConfigurableConstants
import java.nio.ByteBuffer

class PrimitiveCharArray private constructor(size: Int, native: Boolean, internalBuffer: ByteBuffer?) :
    PrimitiveArray<Char>(size, CHAR_SIZE, native, MAX_SIZE, internalBuffer) {
    @JvmOverloads
    constructor(size: Int, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) : this(size, native, null)

    companion object {
        @JvmStatic
        val MAX_SIZE: Int = PrimitiveByteArray.MAX_SIZE / CHAR_SIZE
    }

    override operator fun get(index: Int): Char = internalGetChar(index)

    override operator fun set(index: Int, element: Char) {
        internalSetChar(index, element)
    }

    fun getP(index: Int): Char = internalGetChar(index)

    fun setP(index: Int, element: Char) = internalSetChar(index, element)

    override fun getResizedCopy(difference: Int): PrimitiveArray<Char> {
        val newSize = getNewSize(difference)
        val newBuffer: ByteBuffer = getResizedCopyOfBuffer(newSize)
        return PrimitiveCharArray(newSize, native, newBuffer)
    }
}