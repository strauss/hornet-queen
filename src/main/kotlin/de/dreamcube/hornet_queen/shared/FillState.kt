package de.dreamcube.hornet_queen.shared

import java.util.*

/**
 * Internal representation of fill states for indexes. Uses two [BitSet]s as internal data structure for saving memory space.
 */
class FillState(capacity: Int) {
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