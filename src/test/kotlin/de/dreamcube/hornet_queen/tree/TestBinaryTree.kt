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

package de.dreamcube.hornet_queen.tree

import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBinaryTree {

    private val testData: List<Int> = listOf(100, 50, 150, 25, 1, -1, 2, 75, 1337, 42, 16, 70, 4711, 74, -1000, 32, -32, -16)
    private lateinit var testTree: PrimitiveIntBinaryTree

    @Before
    fun beforeEach() {
        testTree = PrimitiveIntBinaryTree()
    }

    @Test
    fun testAdd() {
        val includedElements: MutableList<Int> = PrimitiveIntArrayList()
        assertInvariants(includedElements)
        testData.forEach {
            testTree.insertKey(it)
            includedElements.add(it)
            assertInvariants(includedElements)
        }
    }

    @Test
    fun testRemove() {
        val includedElements: MutableList<Int> = PrimitiveIntArrayList()
        testData.forEach {
            includedElements.add(it)
            testTree.insertKey(it)
        }
        val testDataCopy = PrimitiveIntArrayList()
        testDataCopy.addAll(testData)
        testDataCopy.shuffle()

        assertInvariants(includedElements)
        testDataCopy.forEach {
            includedElements.remove(it)
            testTree.removeKey(it)
            assertInvariants(includedElements)
        }
    }

    @Test
    fun testIteratorRemove() {
        val includedElements: MutableList<Int> = PrimitiveIntArrayList()
        testData.forEach {
            includedElements.add(it)
            testTree.insertKey(it)
        }
        val testDataCopy = PrimitiveIntArrayList()
        testDataCopy.addAll(testData)
        testDataCopy.sort()
        val removeElements: MutableSet<Int> = PrimitiveIntSetB()
        removeElements.add(testDataCopy[0])
        removeElements.add(testDataCopy[testDataCopy.size / 2 - 1])
        removeElements.add(testDataCopy[testDataCopy.size / 2 + 1])
        removeElements.add(testDataCopy[testDataCopy.size / 2])
        removeElements.add(testDataCopy[testDataCopy.size - 1])

        val indexIterator = testTree.unorderedIndexIterator()
        while (indexIterator.hasNext()) {
            val nextIndex = indexIterator.next()
            val nextElement = testTree.keys[nextIndex]
            if (removeElements.contains(nextElement)) {
                indexIterator.remove()
                includedElements.remove(nextElement)
                assertInvariants(includedElements)
            }
        }
    }

    @Test
    fun testIteratorRemoveAll() {
        val includedElements: MutableList<Int> = PrimitiveIntArrayList()
        testData.forEach {
            includedElements.add(it)
            testTree.insertKey(it)
        }
        includedElements.sort()
        val referenceIterator = includedElements.iterator()
        val indexIterator = testTree.inorderIndexIterator()
        while (referenceIterator.hasNext()) {
            assertTrue(indexIterator.hasNext())
            val reference = referenceIterator.next()
            val nextIndex = indexIterator.next()
            val nextElement = testTree.keys[nextIndex]
            assertEquals(reference, nextElement)
            referenceIterator.remove()
            indexIterator.remove()
            assertInvariants(includedElements)
        }
    }

    private fun assertInvariants(includedElements: List<Int>) {
        assertEquals(includedElements.size, testTree.size)
        assertTrue(testTree.isBalanced())
        includedElements.forEach {
            assertTrue(testTree.containsKey(it))
        }

        val includedCopy = PrimitiveIntArrayList()
        includedCopy.addAll(includedElements)
        Collections.sort(includedCopy)

        // Check if the tree preserves the order after each add
        val referenceIterator = includedCopy.iterator()
        val testIterator = testTree.inorderIndexIterator()
        var i: Int = 0
        while (referenceIterator.hasNext()) {
            assertTrue(testIterator.hasNext())
            val referenceValue = referenceIterator.next()
            val index = testIterator.next()
            val value = testTree.keys[index]
            assertEquals(referenceValue, value)
            i += 1
        }
        assertEquals(includedElements.size, i)

        i = 0
        val unorderedTestIterator = testTree.unorderedIndexIterator()
        while (unorderedTestIterator.hasNext()) {
            val nextIndex = unorderedTestIterator.next()
            val nextElement = testTree.keys[nextIndex]
            assertTrue(includedElements.contains(nextElement))
            i += 1
        }
        assertEquals(includedElements.size, i)

    }

}