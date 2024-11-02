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

import org.junit.Test
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals

private const val TEST_SIZE_FRACTION = 2


class TestPrimitiveArray {

    @Test
    fun testPrimitiveByteArray() {
        val size = PrimitiveByteArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveByteArray(size, false)
        val nativeTestArray = PrimitiveByteArray(size, true)
        val referenceArray = ByteArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = Random.nextInt().toByte()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testPrimitiveShortArray() {
        val size = PrimitiveShortArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveShortArray(size, false)
        val nativeTestArray = PrimitiveShortArray(size, true)
        val referenceArray = ShortArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = Random.nextInt().toShort()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testPrimitiveCharArray() {
        val size = PrimitiveCharArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveCharArray(size, false)
        val nativeTestArray = PrimitiveCharArray(size, true)
        val referenceArray = CharArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue: Char = Random.nextInt().toChar()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testPrimitiveIntArray() {
        val size = PrimitiveIntArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveIntArray(size, false)
        val nativeTestArray = PrimitiveIntArray(size, true)
        val referenceArray = IntArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = Random.nextInt()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testPrimitiveLongArray() {
        val size = PrimitiveLongArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveLongArray(size, false)
        val nativeTestArray = PrimitiveLongArray(size, true)
        val referenceArray = LongArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = Random.nextLong()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testPrimitiveFloatArray() {
        val size = PrimitiveFloatArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveFloatArray(size, false)
        val nativeTestArray = PrimitiveFloatArray(size, true)
        val referenceArray = FloatArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = Random.nextFloat()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testPrimitiveDoubleArray() {
        val size = PrimitiveDoubleArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = PrimitiveDoubleArray(size, false)
        val nativeTestArray = PrimitiveDoubleArray(size, true)
        val referenceArray = DoubleArray(size)

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = Random.nextDouble()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }

    @Test
    fun testUUIDArray() {
        val size = UUIDArray.MAX_SIZE / TEST_SIZE_FRACTION
        val testArray = UUIDArray(size, false)
        val nativeTestArray = UUIDArray(size, true)
        val referenceArray: Array<UUID> = Array(size) { UUID(0L, 0L) }

        // Test size
        assertEquals(referenceArray.size, testArray.size)
        assertEquals(referenceArray.size, nativeTestArray.size)

        // Fill array
        for (i in 0..<size) {
            val nextValue = UUID.randomUUID()
            testArray[i] = nextValue
            nativeTestArray[i] = nextValue
            referenceArray[i] = nextValue
        }

        // Test content
        for (i in 0..<size) {
            assertEquals(referenceArray[i], testArray[i])
            assertEquals(referenceArray[i], nativeTestArray[i])
        }

        // Test reduction
        val reducedSize = size / 2
        val reducedTestArray = testArray.getResizedCopy(-(size - reducedSize))
        val reducedNativeTestArray = nativeTestArray.getResizedCopy(-(size - reducedSize))

        // Test size
        assertEquals(reducedSize, reducedTestArray.size)
        assertEquals(reducedSize, reducedNativeTestArray.size)

        // Test content
        for (i in 0..<reducedSize) {
            assertEquals(referenceArray[i], reducedTestArray[i])
            assertEquals(referenceArray[i], reducedNativeTestArray[i])
        }

        // Test expansion
        val expandBy = 12_111
        val expandedTestArray = reducedTestArray.getResizedCopy(expandBy)
        val expandedNativeTestArray = reducedNativeTestArray.getResizedCopy(expandBy)
        val expandedSize = reducedSize + expandBy

        // Test size
        assertEquals(expandedSize, expandedTestArray.size)
        assertEquals(expandedSize, expandedNativeTestArray.size)

        // fill expanded content with previously generated data
        for (i in reducedSize..<expandedSize) {
            expandedTestArray[i] = referenceArray[i]
            expandedNativeTestArray[i] = referenceArray[i]
        }

        // Test content
        for (i in 0..<expandedSize) {
            assertEquals(referenceArray[i], expandedTestArray[i])
            assertEquals(referenceArray[i], expandedNativeTestArray[i])
        }
    }
}