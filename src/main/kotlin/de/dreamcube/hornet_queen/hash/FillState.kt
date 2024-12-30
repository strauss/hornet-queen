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

import java.util.*

/**
 * Internal representation of fill states for indexes. Uses two [BitSet]s as internal data structure for saving memory space.
 */
internal class FillState(capacity: Int) {
    private var fullSet = BitSet(capacity)
    private var removedSet = BitSet(capacity)

    fun isFree(index: Int): Boolean {
        return !fullSet[index] && !removedSet[index]
    }

    fun isFull(index: Int): Boolean {
        return fullSet[index] && !removedSet[index]
    }

    fun isRemoved(index: Int): Boolean {
        return !fullSet[index] && removedSet[index]
    }

    fun setFree(index: Int) {
        fullSet.clear(index)
        removedSet.clear(index)
    }

    fun setFull(index: Int) {
        fullSet.set(index)
        removedSet.clear(index)
    }

    fun setRemoved(index: Int) {
        fullSet.clear(index)
        removedSet.set(index)
    }
}