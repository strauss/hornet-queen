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

package de.dreamcube.hornet_queen.map

import java.util.*

fun main() {
    // simple example
    val intIntMap: MutableMap<Int, Int> = HashTableBasedMapBuilder
        .useIntKey()
        .useIntValue()
        .create()

    // complex example, parameter names given for clarity
    val uuidStringMap: MutableMap<UUID, String> = HashTableBasedMapBuilder
        .useUUIDKey(native = false)
        .useArbitraryTypeValue<String>()
        .create(initialCapacity = 42, loadFactor = 0.8)
}