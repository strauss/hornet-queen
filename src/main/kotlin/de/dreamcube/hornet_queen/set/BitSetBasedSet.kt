package de.dreamcube.hornet_queen.set

import java.util.*

abstract class BitSetBasedSet<T>(private val toInt: T.() -> Int, private val fromInt: Int.() -> T) :
    PrimitiveMutableSet<T> {
    private val internalBitSet = BitSet()

    override val size
        get() = internalBitSet.cardinality()

    override fun isEmpty(): Boolean = internalBitSet.isEmpty

    private fun containsInt(element: Int): Boolean = internalBitSet.get(element)

    private fun addInt(element: Int) = internalBitSet.set(element)

    private fun removeInt(element: Int) = internalBitSet.set(element, false)

    override fun iterator(): BitSetIterator = BitSetIterator()

    override fun clear() {
        internalBitSet.clear()
    }

    override fun add(element: T): Boolean {
        val elementAsInt: Int = element.toInt()
        val contained = containsInt(elementAsInt)
        addInt(elementAsInt)
        return !contained
    }

    override fun contains(element: T): Boolean = containsInt(element.toInt())

    override fun remove(element: T): Boolean {
        val elementAsInt: Int = element.toInt()
        val contained = containsInt(elementAsInt)
        removeInt(elementAsInt)
        return contained
    }

    inner class BitSetIterator : MutableIterator<T> {

        private var nextProbePosition = 0

        private var nextSetBit = -1

        private var lastDeliveredBit = -1

        override fun hasNext(): Boolean {
            if (nextProbePosition < 0) {
                // happens if MAX_INT is reached and the nextProbePosition overflows
                return false
            }
            internalDetermineNextSetBit()
            return nextProbePosition >= 0 && nextSetBit >= 0
        }

        private fun internalDetermineNextSetBit() {
            if (nextSetBit == -1) {
                nextSetBit = internalBitSet.nextSetBit(nextProbePosition)
            }
        }

        override fun next(): T {
            internalDetermineNextSetBit()
            if (nextSetBit < 0) {
                throw NoSuchElementException()
            }
            nextProbePosition = nextSetBit + 1
            lastDeliveredBit = nextSetBit
            nextSetBit = -1
            return lastDeliveredBit.fromInt()
        }

        override fun remove() {
            if (lastDeliveredBit < 0) {
                throw IllegalStateException()
            }
            internalBitSet.set(lastDeliveredBit, false)
            lastDeliveredBit = -1
        }

    }
}

class PrimitiveByteSetB : BitSetBasedSet<Byte>(Byte::unsignedToInt, Int::toByte)
class PrimitiveShortSetB : BitSetBasedSet<Short>(Short::unsignedToInt, Int::toShort)
class PrimitiveCharSetB : BitSetBasedSet<Char>(Char::unsignedToInt, Int::toChar)
private class PrimitivePositiveIntSetB : BitSetBasedSet<Int>(Int::identity, Int::identity)
private class PrimitiveNegativeIntSetB : BitSetBasedSet<Int>(Int::toPositive, Int::toNegative)
class PrimitiveIntSetB : PrimitiveMutableSet<Int> {

    private val positiveSet = PrimitivePositiveIntSetB()
    private val negativeSet = PrimitiveNegativeIntSetB()

    override val size: Int
        get() = positiveSet.size + negativeSet.size


    override fun add(element: Int): Boolean {
        if (element < 0) {
            return negativeSet.add(element)
        }
        return positiveSet.add(element)

    }

    override fun clear() {
        positiveSet.clear()
        negativeSet.clear()
    }

    override fun isEmpty(): Boolean = positiveSet.isEmpty() && negativeSet.isEmpty()

    override fun iterator(): MutableIterator<Int> {
        return CombinedIterator(positiveSet.iterator(), negativeSet.iterator())
    }

    override fun retainAll(elements: Collection<Int>): Boolean {
        var result = false
        result = positiveSet.retainAll(elements) || result
        result = negativeSet.retainAll(elements) || result
        return result
    }

    override fun remove(element: Int): Boolean {
        if (element < 0) {
            return negativeSet.remove(element)
        }
        return positiveSet.remove(element)
    }

    override fun contains(element: Int): Boolean {
        if (element < 0) {
            return negativeSet.contains(element)
        }
        return positiveSet.contains(element)
    }

}

/**
 * Iterator combining two other iterators. The next-calls are round-robin until either iterator is done. Then only
 * one iterator remains until the whole thing is finished.
 */
class CombinedIterator<T>(firstIterator: MutableIterator<T>, secondIterator: MutableIterator<T>) : MutableIterator<T> {
    private var currentIterator: MutableIterator<T> = if (firstIterator.hasNext()) firstIterator else secondIterator
    private var previousIterator: MutableIterator<T> = secondIterator

    override fun hasNext(): Boolean {
        return currentIterator.hasNext()
    }

    override fun next(): T {
        val result: T = currentIterator.next()
        if (previousIterator.hasNext()) {
            val nextPreviousIterator: MutableIterator<T> = currentIterator
            currentIterator = previousIterator
            previousIterator = nextPreviousIterator
        }
        return result
    }

    override fun remove() {
        previousIterator.remove()
    }

}

private fun Byte.unsignedToInt(): Int = this.toInt() and 0xFF
private fun Short.unsignedToInt(): Int = this.toInt() and 0xFFFF
private fun Char.unsignedToInt(): Int = this.code and 0xFFFF
private fun Int.identity(): Int = this

/**
 * Converts a negative [Int] to a positive [Int] but it is not -i. The negative [Int] is shifted to the right (+1) and
 * then inverted. This maps [Int.MIN_VALUE] to [Int.MAX_VALUE], avoiding overflows. It also maps -1 to 0.
 */
private fun Int.toPositive(): Int = -(this + 1)

/**
 * Converts a positive [Int] to a negative [Int] but it is not -i. The positive [Int] is negated and then shiftet to the
 * left (-1). This maps [Int.MAX_VALUE] to [Int.MIN_VALUE], avoiding overflows. it also maps 0 to -1.
 */
private fun Int.toNegative(): Int = -this - 1
