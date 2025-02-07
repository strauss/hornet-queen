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

import de.dreamcube.hornet_queen.array.PrimitiveByteArray
import kotlin.math.min

internal class ObjectTypeValueCollection<T>(
    size: Int
) : MutableIndexedValueCollection<T> {
    var array: Array<Any?> = Array(size) { null }
        private set

    override val size: Int
        get() = array.size

    override fun get(index: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return array[index] as T
    }

    override fun asCollection(contained: (Int) -> Boolean): MutableCollection<T> {
        val result: MutableList<T> = mutableListOf()
        for (i: Int in array.indices) {
            // we only add elements that are contained
            if (contained(i)) {
                @Suppress("UNCHECKED_CAST")
                result.add(array[i] as T)
            }
        }
        return result
    }

    override fun contains(value: T): Boolean {
        for (element: Any? in array) {
            if (element == value) {
                return true
            }
        }
        return false
    }

    override fun set(index: Int, value: T) {
        array[index] = value
    }

    override fun resize(delta: Int) {
        val newSize: Int = min(size.toLong() + delta.toLong(), PrimitiveByteArray.MAX_SIZE.toLong()).toInt()
        val copyBound = min(newSize, size)
        val newArray = Array<Any?>(newSize) { null }
        System.arraycopy(array, 0, newArray, 0, copyBound)
        array = newArray
    }
}
