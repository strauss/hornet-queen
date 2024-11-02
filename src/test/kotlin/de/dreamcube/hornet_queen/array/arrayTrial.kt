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
    val ia = PrimitiveIntArray(PrimitiveIntArray.MAX_SIZE / 2)
    val nia = PrimitiveIntArray(PrimitiveIntArray.MAX_SIZE / 2)
    for (i in 0..<ia.size) {
        val nextElement = Random.nextInt()
        ia[i] = nextElement
        nia[i] = nextElement
    }

    // Time measurements
    val expandBy = 250_000_000

    var start = System.currentTimeMillis()
    val iac = ia.getResizedCopy(expandBy)
    var stop = System.currentTimeMillis()
    println("Duration not native but arraycopy: ${stop - start} ms.")
    System.gc()

    Thread.sleep(1000L)

    start = System.currentTimeMillis()
    val niac = nia.getResizedCopy(expandBy)
    stop = System.currentTimeMillis()
    println("Duration native but not arraycopy: ${stop - start} ms.")

}