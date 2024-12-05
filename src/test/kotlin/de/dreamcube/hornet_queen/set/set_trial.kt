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

package de.dreamcube.hornet_queen.set

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import java.util.*
import kotlin.random.Random

fun main() {
    val set: MutableSet<Byte> = PrimitiveByteSetB()
    for (i in 1..18) {
        set.add(Random.nextInt(i).toByte())
    }
    val map: MutableMap<UUID, String> = HashTableBasedMapBuilder.useUUIDKey().useArbitraryTypeValue<String>().create()
    for (i in 1..18) {
        map.put(UUID.randomUUID(), UUID.randomUUID().toString())
    }
    println(set)
    println(map)
    println(map.entries)
}