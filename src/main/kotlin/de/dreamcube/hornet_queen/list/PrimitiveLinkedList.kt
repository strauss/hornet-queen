package de.dreamcube.hornet_queen.list

import de.dreamcube.hornet_queen.ConfigurableConstants
import de.dreamcube.hornet_queen.array.*
import java.util.*

private const val NO_INDEX: Int = -1

/**
 * Primitive list with a primitive array as underlying data structure. This variant resembles a usual linked list but
 * without internal node objects containing the actual data and references to the neighbor elements. Instead, the data
 * itself is stored in an array (comparable [PrimitiveArrayList]). The links to the neighbors are stored in
 * [PrimitiveIntArray]s of same length. For most applications a [PrimitiveArrayList] is superior. In some niche
 * applications a [PrimitiveLinkedList] can be more efficient. Compared to the usual [java.util.LinkedList] is better,
 * if the list is filled to a certain limit. The exact computation of this limit depends on the data type [T] and the VM
 * in use. In a [PrimitiveLinkedList] every reference only costs 4 bytes (independently of the VM because of the nature
 * of [PrimitiveArray]s). In a [java.util.LinkedList] every reference costs at least 4 bytes (32 bit VM), but in most
 * cases more (up to 64 bits). Furthermore, a [java.util.LinkedList] requires a wrapper object vor primitive types, and
 * it requires an internal node object for wrapping the wrapper. Using a [PrimitiveLinkedList] instead, you don't need
 * any wrapper objects, and you don't need any inner node objects. The only downside is the expensive copy operation for
 * resizing the internal arrays, as in [PrimitiveArrayList].
 */
abstract class PrimitiveLinkedList<T> protected constructor(
    arraySupplier: (Int) -> PrimitiveArray<T>,
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE
) : PrimitiveArrayBasedList<T>() {
    private var array: PrimitiveArray<T> = arraySupplier(initialSize)
    private var forwardLinks = PrimitiveIntArray(initialSize, array.native)
    private var backwardLinks = PrimitiveIntArray(initialSize, array.native)

    private var firstEmptyIndex = 0
    private var lastEmptyIndex = initialSize - 1
    private var firstElementIndex = NO_INDEX
    private var lastElementIndex = NO_INDEX

    init {
        markAsEmpty()
    }

    private fun markAsEmpty(startIndex: Int = 0) {
        if (startIndex !in 0..<array.size) {
            throw IndexOutOfBoundsException(startIndex)
        }
        firstEmptyIndex = startIndex
        lastEmptyIndex = array.size - 1
        if (startIndex == 0) {
            firstElementIndex = NO_INDEX
            lastElementIndex = NO_INDEX
            size = 0
        }
        val nextIndex = startIndex + 1
        forwardLinks[startIndex] = nextIndex
        backwardLinks[startIndex] = if (startIndex == 0) NO_INDEX else lastEmptyIndex
        for (i in nextIndex..<array.size) {
            forwardLinks[i] = i + 1
            backwardLinks[i] = i - 1
        }
    }

    override var size = 0
        protected set

    private fun grow() {
        val oldCapacity: Int = array.size
        val newCapacity: Int = if (oldCapacity == 0) {
            ConfigurableConstants.DEFAULT_INITIAL_SIZE
        } else {
            calculateNewCapacity(oldCapacity)
        }
        val delta = newCapacity - oldCapacity
        array = array.getResizedCopy(delta)
        forwardLinks = forwardLinks.getResizedCopy(delta)
        backwardLinks = backwardLinks.getResizedCopy(delta)
        markAsEmpty(oldCapacity)
    }

    override fun trimToSize() {
        val theSize = size
        val sizeDifference = size - array.size
        val newArray = array.getResizedCopy(sizeDifference)
        var i = 0
        for (element: T in this) {
            newArray[i] = element
            i += 1
        }
        forwardLinks = forwardLinks.getResizedCopy(sizeDifference)
        backwardLinks = backwardLinks.getResizedCopy(sizeDifference)
        array = newArray
        markAsEmpty()
        size = theSize
        firstElementIndex = 0
        lastElementIndex = size - 1
        forwardLinks[lastElementIndex] = NO_INDEX
        firstEmptyIndex = size
        lastEmptyIndex = NO_INDEX
    }

    override fun getInternalArraySize(): Int = array.size

    override fun getInternalArrayMaxSize(): Int = array.maxSize

    override fun add(element: T): Boolean {
        if (size == array.size) {
            grow()
        }
        internalAppend(element)
        return true
    }

    private fun internalAppend(element: T) {
        if (isEmpty()) {
            internalPrepend(element)
            return
        }
        val currentIndex = firstEmptyIndex
        firstEmptyIndex = forwardLinks[firstEmptyIndex]
        val previousIndex = lastElementIndex
        val nextIndex = forwardLinks[currentIndex] // points to next free index
        array[currentIndex] = element
        backwardLinks[currentIndex] = previousIndex
        if (previousIndex != NO_INDEX) {
            forwardLinks[previousIndex] = currentIndex
        }
        forwardLinks[currentIndex] = NO_INDEX
        if (nextIndex < array.size) {
            backwardLinks[nextIndex] = NO_INDEX
        } else {
            lastEmptyIndex = NO_INDEX
        }
        lastElementIndex = currentIndex
        size += 1
    }

    private fun internalPrepend(element: T) {
        val initiallyEmpty: Boolean = isEmpty()
        val currentIndex = firstEmptyIndex
        firstEmptyIndex = forwardLinks[firstEmptyIndex]
        val nextIndex = firstElementIndex
        array[currentIndex] = element
        firstElementIndex = currentIndex
        forwardLinks[currentIndex] = nextIndex
        if (initiallyEmpty) {
            lastElementIndex = currentIndex
        } else {
            backwardLinks[nextIndex] = currentIndex
        }
        if (firstEmptyIndex < array.size) {
            backwardLinks[firstEmptyIndex] = NO_INDEX
        }
        backwardLinks[currentIndex] = NO_INDEX
        size += 1
    }

    override fun add(index: Int, element: T) {
        if (size == array.size) {
            grow()
        }
        when (index) {
            0 -> {
                // first case: index = 0 a.k.a. "start of list"
                internalPrepend(element)
            }

            size -> {
                // second case: index = size a.k.a. "end of list"
                internalAppend(element)
            }

            else -> {
                // third case: index is something in between
                val internalIndex: Int = seekInternalIndex(index)
                internalAdd(internalIndex, element)
            }
        }
    }

    private fun internalAdd(internalIndex: Int, element: T) {
        // We assume adding "in between", so we do not care about the list of empty space.
        // That one is handled in the internal append and internal prepend functions.
        assert(isNotEmpty())
        assert(backwardLinks[internalIndex] != NO_INDEX)
        assert(internalIndex >= 0)
        assert(internalIndex < array.size)

        val targetIndex = firstEmptyIndex
        firstEmptyIndex = forwardLinks[firstEmptyIndex]
        val previousIndex = backwardLinks[internalIndex]

        array[targetIndex] = element
        forwardLinks[targetIndex] = forwardLinks[previousIndex]
        forwardLinks[previousIndex] = targetIndex
        backwardLinks[targetIndex] = previousIndex
        backwardLinks[internalIndex] = targetIndex
        backwardLinks[firstEmptyIndex] = NO_INDEX
        size += 1
    }

    override fun removeAt(index: Int): T {
        return when (index) {
            0 -> {
                internalRemoveFirst()
            }

            size - 1 -> {
                internalRemoveLast()
            }

            else -> {
                internalRemoveAt(seekInternalIndex(index))
            }
        }
    }

    private fun internalRemoveFirst(): T {
        val result = array[firstElementIndex]
        val nextEmptyIndex = firstEmptyIndex
        firstEmptyIndex = firstElementIndex
        firstElementIndex = forwardLinks[firstElementIndex]
        forwardLinks[firstEmptyIndex] = nextEmptyIndex
        backwardLinks[firstElementIndex] = NO_INDEX
        backwardLinks[nextEmptyIndex] = firstEmptyIndex
        backwardLinks[firstEmptyIndex] = NO_INDEX // should already be the case
        size -= 1
        return result
    }

    private fun internalRemoveLast(): T {
        val result = array[lastElementIndex]
        val nextEmptyIndex = firstEmptyIndex
        firstEmptyIndex = lastElementIndex
        lastElementIndex = backwardLinks[lastElementIndex]
        forwardLinks[firstEmptyIndex] = nextEmptyIndex
        forwardLinks[lastElementIndex] = NO_INDEX
        backwardLinks[nextEmptyIndex] = firstEmptyIndex
        backwardLinks[firstEmptyIndex] = NO_INDEX // should already be the case
        size -= 1
        return result
    }

    private fun internalRemoveAt(internalIndex: Int): T {
        assert(isNotEmpty())
        assert(backwardLinks[internalIndex] != NO_INDEX)
        assert(forwardLinks[internalIndex] != NO_INDEX)

        val result = array[internalIndex]
        val previousIndex = backwardLinks[internalIndex]
        val nextIndex = forwardLinks[internalIndex]
        forwardLinks[previousIndex] = nextIndex
        backwardLinks[nextIndex] = previousIndex

        val nextEmptyIndex = firstEmptyIndex
        firstEmptyIndex = internalIndex
        forwardLinks[firstEmptyIndex] = nextEmptyIndex
        backwardLinks[nextEmptyIndex] = firstEmptyIndex
        backwardLinks[firstEmptyIndex] = NO_INDEX

        size -= 1
        return result
    }

    private fun seekInternalIndex(index: Int): Int {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        return if (index <= size / 2) {
            var currentIndex = firstElementIndex
            for (seekIndex in 0..<index) {
                currentIndex = forwardLinks[currentIndex]
                check(currentIndex != NO_INDEX) { "Error while determining the internal index of $index" }
            }
            currentIndex
        } else {
            var currentIndex = lastElementIndex
            val limit = size - 1 - index
            for (seekIndex in 0..<limit) {
                currentIndex = backwardLinks[currentIndex]
                check(currentIndex != NO_INDEX) { "Error while determining the internal index of $index" }
            }
            currentIndex
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(index)
        }
        if (elements.isEmpty()) {
            return false
        }
        if (index == size) {
            return addAll(elements)
        }
        while (size + elements.size > array.size) {
            grow()
        }
        val iterator = listIterator()
        // skip until index
        for (i: Int in 0..<index) {
            iterator.next()
        }
        for (currentElement: T in elements) {
            iterator.add(currentElement)
        }
        return true
    }

    override fun clear() {
        markAsEmpty()
        size = 0
    }

    override fun get(index: Int): T = array[seekInternalIndex(index)]

    override fun isEmpty(): Boolean = firstElementIndex == NO_INDEX && lastElementIndex == NO_INDEX

    override fun iterator(): MutableIterator<T> = PrimitiveLinkedListIterator()

    override fun listIterator(): MutableListIterator<T> = PrimitiveLinkedListIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = PrimitiveLinkedListIterator(index)

    override fun rangedListIterator(startIndex: Int, endIndex: Int): MutableListIterator<T> =
        PrimitiveLinkedListIterator(startIndex, endIndex)

    override fun set(index: Int, element: T): T {
        val internalIndex: Int = seekInternalIndex(index)
        val result: T = array[internalIndex]
        array[internalIndex] = element
        return result
    }

    override fun lastIndexOf(element: T): Int {
        if (isEmpty()) {
            return -1
        }
        var index: Int = size - 1
        var internalIndex = lastElementIndex
        while (index >= 0) {
            val currentElement: T = array[internalIndex]
            if (currentElement == element) {
                return index
            }
            index -= 1
            internalIndex = backwardLinks[internalIndex]
        }
        return -1
    }

    override fun indexOf(element: T): Int {
        if (isEmpty()) {
            return -1
        }
        var index = 0
        var internalIndex = firstElementIndex
        while (index < size) {
            val currentElement: T = array[internalIndex]
            if (currentElement == element) {
                return index
            }
            index += 1
            internalIndex = forwardLinks[internalIndex]
        }
        return -1
    }

    /**
     * [MutableListIterator] for [PrimitiveLinkedList] for iterating from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     */
    inner class PrimitiveLinkedListIterator(startIndex: Int = 0, endIndex: Int = size) :
        AbstractArrayBasedListIterator<T>(startIndex, endIndex) {

        private var nextInternalIndex: Int = NO_INDEX
        private var lastInternalNextCallIndex: Int = NO_INDEX

        init {
            if (isNotEmpty()) {
                nextInternalIndex = seekInternalIndex(nextIndex)
                checkBounds(startIndex, endIndex, size)
            }
        }

        override fun add(element: T) {
            if (size == array.size) {
                grow()
            }
            when (nextInternalIndex) {
                firstElementIndex -> {
                    internalPrepend(element)
                }

                firstEmptyIndex, NO_INDEX -> {
                    internalAppend(element)
                }

                else -> {
                    internalAdd(nextInternalIndex, element)
                }
            }
            nextIndex += 1
            endIndex += 1
            callState = IteratorCallState.ADD
        }

        override fun next(): T {
            val result: T = array[nextInternalIndex]
            lastInternalNextCallIndex = nextInternalIndex
            incIndex()
            callState = IteratorCallState.NEXT
            return result
        }

        override fun previous(): T {
            decIndex()
            callState = IteratorCallState.PREV
            return array[nextInternalIndex]
        }

        override fun remove() {
            when (callState) {
                IteratorCallState.NEXT -> {
                    internalRemove(previousInternalIndex())
                    nextIndex -= 1
                }

                IteratorCallState.PREV -> {
                    // when previous was called, the next internal index is placed at the element previously returned
                    val removeAt = nextInternalIndex
                    nextInternalIndex = forwardLinks[nextInternalIndex]
                    internalRemove(removeAt)
                }

                else -> error("$callState")
            }
            callState = IteratorCallState.REM
        }

        private fun internalRemove(internalIndex: Int) {
            endIndex -= 1
            when (internalIndex) {
                firstElementIndex -> internalRemoveFirst()
                lastElementIndex -> internalRemoveLast()
                else -> internalRemoveAt(internalIndex)
            }
        }

        override fun set(element: T) {
            when (callState) {
                IteratorCallState.NEXT -> {
                    array[previousInternalIndex()] = element
                }

                IteratorCallState.PREV -> {
                    array[nextInternalIndex] = element
                }

                else -> error("$callState")
            }
            callState = IteratorCallState.SET
        }

        private fun incIndex() {
            nextInternalIndex = forwardLinks[nextInternalIndex]
            nextIndex += 1
        }

        private fun decIndex() {
            nextInternalIndex = previousInternalIndex()
            nextIndex -= 1
        }

        private fun previousInternalIndex(): Int =
            if (nextInternalIndex == NO_INDEX) lastElementIndex else backwardLinks[nextInternalIndex]

    }

}

class PrimitiveByteLinkedList(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveLinkedList<Byte>({ size: Int -> PrimitiveByteArray(size, native) }, initialSize)

class PrimitiveShortLinkedList(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) :
    PrimitiveLinkedList<Short>({ size: Int -> PrimitiveShortArray(size, native) }, initialSize)

class PrimitiveIntLinkedList(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveLinkedList<Int>({ size: Int -> PrimitiveIntArray(size, native) }, initialSize)

class PrimitiveLongLinkedList(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveLinkedList<Long>({ size: Int -> PrimitiveLongArray(size, native) }, initialSize)

class PrimitiveFloatLinkedList(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) :
    PrimitiveLinkedList<Float>({ size: Int -> PrimitiveFloatArray(size, native) }, initialSize)

class PrimitiveDoubleLinkedList(
    initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE,
    native: Boolean = ConfigurableConstants.DEFAULT_NATIVE
) :
    PrimitiveLinkedList<Double>({ size: Int -> PrimitiveDoubleArray(size, native) }, initialSize)

class UUIDLinkedList(initialSize: Int = ConfigurableConstants.DEFAULT_INITIAL_SIZE, native: Boolean = ConfigurableConstants.DEFAULT_NATIVE) :
    PrimitiveLinkedList<UUID>({ size: Int -> UUIDArray(size, native) }, initialSize)
