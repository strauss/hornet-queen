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

import de.dreamcube.hornet_queen.hash.PrimitiveTypeHashTable

class HashTableSetIterator<T>(private val hashTable: PrimitiveTypeHashTable<T, *>) : MutableIterator<T> {
    private var expectedSize: Int = hashTable.size
    internal var lastDeliveredIndex = -1

    private val indexIterator: Iterator<Int> = hashTable.iterator()

    override fun hasNext(): Boolean = indexIterator.hasNext()

    override fun next(): T {
        modificationCheck()
        lastDeliveredIndex = indexIterator.next()
        return hashTable[lastDeliveredIndex]
    }

    override fun remove() {
        modificationCheck()
        if (lastDeliveredIndex < 0) {
            throw IllegalStateException()
        }
        hashTable.removeKeyAt(lastDeliveredIndex)
        lastDeliveredIndex = -1
        expectedSize -= 1
    }

    private fun modificationCheck() {
        if (expectedSize != hashTable.size) {
            throw ConcurrentModificationException()
        }
    }
}