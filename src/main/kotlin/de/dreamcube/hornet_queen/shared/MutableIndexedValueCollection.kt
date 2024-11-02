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

import de.dreamcube.hornet_queen.array.PrimitiveArray
import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable

/**
 * This interface is meant to wrap an array-like structure for values if this [PrimitiveTypeHashTable] is used as
 * foundation for a map implementation. This generic approach allows for primitive values in [PrimitiveArray]s and
 * also for object type values in regular Object arrays. The functions [get], [set], and [contains] are as you would
 * expect them to be in a regular array or collection.
 */
interface MutableIndexedValueCollection<V> {
    val size: Int
    val fillState: FillState
    operator fun get(index: Int): V?
    operator fun set(index: Int, value: V)
    fun contains(value: V): Boolean

    /**
     * Copies all values into a [MutableCollection]. Changes to this collection will not affect this
     * [PrimitiveTypeHashTable].
     */
    fun asCollection(): MutableCollection<V>
}
