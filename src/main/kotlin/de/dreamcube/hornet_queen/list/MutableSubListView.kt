package de.dreamcube.hornet_queen.list

/**
 * Implementation of a sub list view. All read operations are mapped to the given [list]. All write operations, except
 * [set], are not implemented ([UnsupportedOperationException]) because this sub list view does not support structural
 * changes to the underlying list. The range of the view never changes. If the underlying list shrinks below the
 * [upperIndex], accessing elements might trigger an [IndexOutOfBoundsException].
 */
class MutableSubListView<T>(
    private val list: PrimitiveArrayBasedList<T>,
    private val lowerIndex: Int,
    private val upperIndex: Int
) : PrimitiveArrayBasedList<T>() {
    init {
        checkBounds(lowerIndex, upperIndex, list.size)
    }

    override val size: Int = upperIndex - lowerIndex

    override fun clear() {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun addAll(elements: Collection<T>): Boolean {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun add(index: Int, element: T) {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun add(element: T): Boolean {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun get(index: Int): T {
        val actualIndex: Int = getIndexInActualList(index)
        return list[actualIndex]
    }

    private fun getIndexInActualList(index: Int): Int {
        val actualIndex: Int = lowerIndex + index
        if (actualIndex !in lowerIndex..<upperIndex) {
            throw IndexOutOfBoundsException(index)
        }
        return actualIndex
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): MutableIterator<T> = list.rangedListIterator(lowerIndex, upperIndex)

    override fun listIterator(): MutableListIterator<T> = list.rangedListIterator(lowerIndex, upperIndex)

    override fun listIterator(index: Int): MutableListIterator<T> =
        list.rangedListIterator(lowerIndex + index, upperIndex)

    override fun rangedListIterator(startIndex: Int, endIndex: Int): MutableListIterator<T> =
        list.rangedListIterator(lowerIndex + startIndex, lowerIndex + endIndex)

    override fun removeAt(index: Int): T {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun trimToSize() {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun getInternalArraySize(): Int = list.getInternalArraySize()

    override fun getInternalArrayMaxSize(): Int = list.getInternalArrayMaxSize()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return MutableSubListView(this, fromIndex, toIndex)
    }

    override fun set(index: Int, element: T): T {
        val actualIndex: Int = getIndexInActualList(index)
        val result = list[actualIndex]
        list[actualIndex] = element
        return result
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun remove(element: T): Boolean {
        throw UnsupportedOperationException("No structural changes allowed on sub list view.")
    }

    override fun lastIndexOf(element: T): Int {
        val foundIndex = list.lastIndexOf(element)
        return if (foundIndex in lowerIndex..<upperIndex) foundIndex else -1
    }

    override fun indexOf(element: T): Int {
        val foundIndex = list.indexOf(element)
        return if (foundIndex in lowerIndex..<upperIndex) foundIndex else -1
    }

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }

    override fun contains(element: T): Boolean = indexOf(element) >= 0

}