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

package de.dreamcube.hornet_queen.hash

import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import java.util.*

fun main() {
    val selectedPrimes: List<Int> = selectPrimes()
    println(selectedPrimes)
}

/**
 * <p>
 * The core of this approach is an implementation by CERN from 1999. It uses so-called "chunks". Each chunk consists of
 * a start prime and derived primes, where each subsequent prime is at least twice as large as the previous one. The
 * selection of start prime values was determined manually at CERN by Wolfgang Hoschek. I am just using their start
 * values and calculate the other primes myself.
 * </p>
 * <p>
 * This class is placed next to the test cases because it is something in between a code generator and a static array
 * definition. I ran it once with the parameters I prefer and copy the resulting array into the object [PrimeProvider].
 * The binary library will only contain the computed primes, not the code that created them.
 * </p>
 */
internal fun selectPrimes(): List<Int> {
    println("Calculating all primes up to maximum integer limit (this can take some time).")
    val primesBySieve: BitSetResultStructure = primesBySieve(Int.MAX_VALUE)

    val alreadyIncludedPrimes = BitSet(Int.MAX_VALUE)
    alreadyIncludedPrimes.set(Int.MAX_VALUE) // largest prime is always included

    val startPrimes: List<Int> = listOf(3, 5, 31, 43, 311, 379, 433, 599, 953, 1039)

    for (currentValue: Int in startPrimes) {
        calculateChunk(currentValue, primesBySieve, alreadyIncludedPrimes)
    }
    println("Selected ${alreadyIncludedPrimes.cardinality()} prime numbers.")
    val selectedPrimes: List<Int> = transformToList(alreadyIncludedPrimes)
    return selectedPrimes
}

private fun transformToList(alreadyIncludedPrimes: BitSet): List<Int> {
    val selectedPrimes = PrimitiveIntArrayList(alreadyIncludedPrimes.cardinality())
    var i = alreadyIncludedPrimes.nextSetBit(0)
    while (i >= 0) {
        selectedPrimes.add(i)
        if (i == Int.MAX_VALUE) {
            break
        }
        i = alreadyIncludedPrimes.nextSetBit(i + 1)
    }
    return selectedPrimes
}

private fun calculateChunk(startValue: Int, primes: BitSetResultStructure, alreadyIncludedPrimes: BitSet) {
    val startPrime = primes.nextPrime(startValue)
    var currentPrime = startPrime
    alreadyIncludedPrimes.set(currentPrime)
    while (true) {
        if (!alreadyIncludedPrimes.get(currentPrime)) {
            alreadyIncludedPrimes.set(currentPrime)
        }
        val nextSmallestPrimeCandidate: Int = currentPrime shl 1
        if (nextSmallestPrimeCandidate > 0) {
            currentPrime = primes.nextPrime(nextSmallestPrimeCandidate)
        } else {
            break // do it until overflow
        }
    }
}