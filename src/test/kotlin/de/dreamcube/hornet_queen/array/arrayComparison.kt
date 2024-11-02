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

import kotlin.random.Random

fun main() {
    val elements = 250_000_000
    println("Elements: $elements")
    println()
    println("LongArray")
    var start = System.currentTimeMillis()
    val ia = LongArray(elements)
    for (i in 0..<elements) {
        ia[i] = Random.nextInt().toLong()
    }
    var x = 0L
    for (i: Long in ia) {
        x += i
    }
    var stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()
    println("PrimitiveArrayWithConverters")
    start = System.currentTimeMillis()
    val pa: PrimitiveArrayWithConverters<Long> = PrimitiveArrayWithConverters(
        elements,
        8,
        PrimitiveArrayConverters::longOutConverter,
        PrimitiveArrayConverters::longInConverter
    )
    for (i in 0..<elements) {
        pa[i] = Random.nextInt().toLong()
    }
    x = 0
    for (d: Long in pa) {
        x += d
    }
    stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()
    println("PrimitiveLongArray")
    start = System.currentTimeMillis()
    val pia = PrimitiveLongArray(elements)
    for (i in 0..<elements) {
        pia[i] = Random.nextInt().toLong()
    }
    x = 0
    for (d: Long in pia) {
        x += d
    }
    stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()

    println("PrimitiveLongArray (direct allocation)")
    start = System.currentTimeMillis()
    val ppia = PrimitiveLongArray(elements)
    for (i in 0..<elements) {
        ppia[i] = Random.nextInt().toLong()
    }
    x = 0
    for (d: Long in ppia) {
        x += d
    }
    stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()
}