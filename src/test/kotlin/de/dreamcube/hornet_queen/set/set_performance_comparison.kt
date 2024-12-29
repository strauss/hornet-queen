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

import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList
import java.util.*
import kotlin.random.Random

fun main() {
    val elementCount = 5_000_000
    uuidPerformanceTest(elementCount)
    longPerformanceTest(elementCount)
    intPerformanceTest(elementCount)
}

fun uuidPerformanceTest(elementCount: Int) {
    println("UUID")
    // generate a lot of elements
    val elements: List<UUID> = (Array(elementCount) { UUID.randomUUID() }).toList()
    val notElements: List<UUID> = (Array(elementCount) { UUID.randomUUID() }).toList()

    println("Elements: $elementCount")
    println()

    comparisonSetTest(elements, notElements, { HashSet() }, { UUIDSet() })
    println("-----")
    println()
}

private fun longPerformanceTest(elementCount: Int) {
    println("Long")
    val elementSet: MutableSet<Long> = mutableSetOf()
    for (i in 1..elementCount) {
        var nextElement: Long = Random.nextLong()
        while (elementSet.contains(nextElement)) {
            nextElement = Random.nextLong()
        }
        elementSet.add(Random.nextLong())
    }
    val notElements: MutableList<Long> = PrimitiveLongArrayList(elementCount)
    for (i in 1..elementCount) {
        var nextElement: Long = Random.nextLong()
        while (elementSet.contains(nextElement)) {
            nextElement = Random.nextLong()
        }
        notElements.add(nextElement)
    }
    val elements: MutableList<Long> = PrimitiveLongArrayList(elementCount)
    elements.addAll(elementSet)
    comparisonSetTest(elements, notElements, { HashSet() }, { PrimitiveLongSet() }, { TreeSet() }, { PrimitiveLongTreeSet(maxHeightDifference = 1) })
    println("-----")
    println()
}

private fun intPerformanceTest(elementCount: Int) {
    println("Int")
    val elementSet: MutableSet<Int> = mutableSetOf()
    for (i in 1..elementCount) {
        var element = Random.nextInt()
        while (elementSet.contains(element)) {
            element = Random.nextInt()
        }
        elementSet.add(element)
    }
    val notElements: MutableList<Int> = PrimitiveIntArrayList(elementCount)
    for (i in 1..elementCount) {
        var nextElement: Int = Random.nextInt()
        while (elementSet.contains(nextElement)) {
            nextElement = Random.nextInt()
        }
        notElements.add(nextElement)
    }
    val elements: MutableList<Int> = PrimitiveIntArrayList(elementCount)
    elements.addAll(elementSet)
    comparisonSetTest(elements, notElements, { HashSet() }, { PrimitiveIntSet() }, { PrimitiveIntSetB() })
    println("-----")
    println()
}

private fun <T> comparisonSetTest(elements: List<T>, notElements: List<T>, vararg sets: () -> MutableSet<T>) {
    for (setSupplier: () -> MutableSet<T> in sets) {
        val set: MutableSet<T> = setSupplier()
        println(set.javaClass.canonicalName)
        setPerformanceTest(set, elements, notElements)
    }
}

fun <T> setPerformanceTest(theSet: MutableSet<T>, elements: List<T>, notElements: List<T>) {
    theSet.clear()

    val startAdd: Long = System.currentTimeMillis()
    for (current: T in elements) {
        theSet.add(current)
    }
    val stopAdd: Long = System.currentTimeMillis()
    println("Add: ${stopAdd - startAdd} ms")

    var containsAll = true
    val startContains: Long = System.currentTimeMillis()
    for (current: T in elements) {
        val contained: Boolean = theSet.contains(current)
        containsAll = contained && containsAll
    }
    val stopContains: Long = System.currentTimeMillis()
    println("Contains(+): ${stopContains - startContains} ms")

    val startNotContains: Long = System.currentTimeMillis()
    var containsNone = true
    for (current: T in notElements) {
        containsNone = !theSet.contains(current) && containsNone
    }
    val stopNotContains: Long = System.currentTimeMillis()
    println("Contains(-): ${stopNotContains - startNotContains} ms")

    val someList: MutableList<T> = ArrayList(elements.size)
    val startIterator: Long = System.currentTimeMillis()
    for (uuid: T in theSet) {
        someList.add(uuid)
    }
    val stopIterator: Long = System.currentTimeMillis()
    println("Iterator: ${stopIterator - startIterator} ms (${someList.size})")

    println()
}