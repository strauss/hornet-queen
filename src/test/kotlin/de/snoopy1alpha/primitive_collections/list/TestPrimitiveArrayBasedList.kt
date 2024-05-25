package de.snoopy1alpha.primitive_collections.list

import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(value = Parameterized::class)
class TestPrimitiveArrayBasedList(private val testListConstructor: () -> PrimitiveArrayBasedList<Long>) {


    private lateinit var testList: PrimitiveArrayBasedList<Long>
    private lateinit var referenceList: ArrayList<Long>

    companion object {
        private const val TEST_SIZE = 25000
        private val testData = LongArray(TEST_SIZE)

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> = arrayListOf(
            arrayOf({ PrimitiveLongArrayList() }),
            arrayOf({ PrimitiveLongLinkedList() })
        )

        @JvmStatic
        @BeforeClass
        fun generateTestData() {
            testData.indices.forEach {
                val nextElement = Random.nextLong()
                testData[it] = nextElement
            }
        }
    }

    @Before
    fun beforeEach() {
        // fill List and reference List with test data
        testList = testListConstructor()
        referenceList = ArrayList()
        for (element: Long in testData) {
            testList.add(element)
            referenceList.add(element)
            assertEquals(referenceList.size, testList.size)
        }
    }

    private fun randomIndex(): Int {
        val randomIndex = abs(Random.nextInt()) % referenceList.size
        if (randomIndex == 0) {
            return 1
        }
        if (randomIndex == referenceList.size - 1) {
            return referenceList.size - 2
        }
        return randomIndex
    }

    private fun additionalList() = listOf(System.currentTimeMillis(), 0L, 1L, 1337L)

    private fun assertWithReferenceList(
        referenceList: List<Long> = this.referenceList,
        testList: List<Long> = this.testList
    ) {
        assertEquals(referenceList.size, testList.size)

        // iterate forwards
        val listIterator = testList.listIterator()
        var i = 0
        while (listIterator.hasNext()) {
            val current = listIterator.next()
            assertEquals(referenceList[i], current, "Elements don't match at index $i.")
            i += 1
        }

        // iterate backwards
        while (listIterator.hasPrevious()) {
            i -= 1
            val current = listIterator.previous()
            assertEquals(referenceList[i], current, "Elements don't match at index $i.")
        }
    }

    @Test
    fun testRemoveElement() {
        val notContainedElement: Long = randomNotContainedElement()

        assertFalse(testList.remove(notContainedElement))

        val containedElement: Long = referenceList[randomIndex()]
        referenceList.remove(containedElement)
        assertTrue(testList.remove(containedElement))

        assertWithReferenceList()
    }

    @Test
    fun testRemoveAll() {
        val elements: List<Long> = listOf(
            randomContainedElement(),
            randomContainedElement(),
            randomContainedElement(),
            randomNotContainedElement(),
            randomContainedElement()
        )

        referenceList.removeAll(elements)
        testList.removeAll(elements)

        assertWithReferenceList()
    }

    @Test
    fun testRetainAll() {
        val elements: List<Long> = listOf(
            randomContainedElement(),
            randomContainedElement(),
            randomContainedElement(),
            randomNotContainedElement(),
            randomContainedElement()
        )
        referenceList.retainAll(elements)
        testList.retainAll(elements)

        assertWithReferenceList()
    }

    private fun randomContainedElement(): Long = referenceList[randomIndex()]

    private fun randomNotContainedElement(): Long {
        var notContainedElement: Long = Random.nextLong()
        while (referenceList.contains(notContainedElement)) {
            notContainedElement = Random.nextLong()
        }
        return notContainedElement
    }

    @Test
    fun testRemoveAtIndex() {
        val randomIndex: Int = randomIndex()
        referenceList.removeAt(randomIndex)
        testList.removeAt(randomIndex)
        assertWithReferenceList()

        referenceList.removeAt(0)
        testList.removeAt(0)
        assertWithReferenceList()
    }

    @Test
    fun testSet() {
        val newValue: Long = Random.nextLong()
        val testIndex: Int = randomIndex()

        var oldValueReference: Long = referenceList.set(testIndex, newValue)
        var oldValue: Long = testList.set(testIndex, newValue)

        assertEquals(oldValueReference, oldValue)

        oldValueReference = referenceList.set(0, newValue)
        oldValue = testList.set(0, newValue)

        assertEquals(oldValueReference, oldValue)

        oldValueReference = referenceList.set(referenceList.size - 1, newValue)
        oldValue = testList.set(testList.size - 1, newValue)

        assertEquals(oldValueReference, oldValue)

        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList[-1] = newValue }
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList[testList.size] }

        assertWithReferenceList()
    }

    @Test
    fun testGet() {
        val testIndex: Int = randomIndex()
        assertEquals(referenceList[testIndex], testList[testIndex])
    }

    @Test
    fun testTrimToSize() {
        assertNotEquals(TEST_SIZE, testList.getInternalArraySize())
        testList.trimToSize()
        assertEquals(TEST_SIZE, testList.getInternalArraySize())
        assertWithReferenceList(referenceList, testList)

        // Resize test
        val newElement: Long = Random.nextLong()
        referenceList.add(newElement)
        testList.add(newElement)
        assertEquals(TEST_SIZE + (TEST_SIZE shr 1), testList.getInternalArraySize())
        assertWithReferenceList(referenceList, testList)
    }

    @Test
    fun testAddAtIndex() {
        val newValue: Long = Random.nextLong()
        val randomIndex: Int = randomIndex()

        referenceList.add(randomIndex, newValue)
        testList.add(randomIndex, newValue)
        assertWithReferenceList()

        val lastIndex: Int = TEST_SIZE
        referenceList.add(lastIndex, newValue)
        testList.add(lastIndex, newValue)
        assertWithReferenceList()

        val sizeAsIndex: Int = referenceList.size
        referenceList.add(sizeAsIndex, newValue)
        testList.add(sizeAsIndex, newValue)
        assertWithReferenceList()

        referenceList.add(0, newValue)
        testList.add(0, newValue)
        assertWithReferenceList()

        val exceedIndex: Int = referenceList.size + 1
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList.add(exceedIndex, newValue) }
    }

    @Test
    fun testAddAll() {
        val additionalList: List<Long> = additionalList()
        referenceList.addAll(additionalList)
        testList.addAll(additionalList)
        assertWithReferenceList()
    }

    @Test
    fun testAddAllAtIndex() {
        val additionalList: List<Long> = additionalList()
        val randomIndex: Int = randomIndex()

        referenceList.addAll(randomIndex, additionalList)
        testList.addAll(randomIndex, additionalList)
        assertWithReferenceList()

        val lastIndex: Int = TEST_SIZE
        referenceList.addAll(lastIndex, additionalList)
        testList.addAll(lastIndex, additionalList)
        assertWithReferenceList()

        val sizeAsIndex: Int = referenceList.size
        referenceList.addAll(sizeAsIndex, additionalList)
        testList.addAll(sizeAsIndex, additionalList)
        assertWithReferenceList()

        referenceList.addAll(0, additionalList)
        testList.addAll(0, additionalList)
        assertWithReferenceList()

        val exceedIndex: Int = referenceList.size + 1
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList.addAll(exceedIndex, additionalList) }
    }

    @Test
    fun testClear() {
        referenceList.clear()
        testList.clear()
        assertWithReferenceList()

        referenceList.add(1L)
        testList.add(1L)
        assertWithReferenceList()
    }

    @Test
    fun testIndexFunctions() {
        val randomContainedElement: Long = referenceList[randomIndex()]
        assertEquals(referenceList.indexOf(randomContainedElement), testList.indexOf(randomContainedElement))
        assertEquals(referenceList.lastIndexOf(randomContainedElement), testList.indexOf(randomContainedElement))

        val randomNotContainedElement: Long = randomNotContainedElement()
        assertEquals(referenceList.indexOf(randomNotContainedElement), testList.indexOf(randomNotContainedElement))
        assertEquals(
            referenceList.lastIndexOf(randomNotContainedElement), testList.lastIndexOf(randomNotContainedElement)
        )

        val firstElement: Long = referenceList[0]
        assertEquals(referenceList.indexOf(firstElement), testList.indexOf(firstElement))
        assertEquals(referenceList.lastIndexOf(firstElement), testList.lastIndexOf(firstElement))

        val lastElement: Long = referenceList[referenceList.size - 1]
        assertEquals(referenceList.indexOf(lastElement), testList.indexOf(lastElement))
        assertEquals(referenceList.lastIndexOf(lastElement), testList.lastIndexOf(lastElement))
    }

    @Test
    fun testListIterator() {
        // the usual
        var referenceIterator: MutableListIterator<Long> = referenceList.listIterator()
        var testIterator: MutableListIterator<Long> = testList.listIterator()
        assertIteratorUntilEnd(referenceIterator, testIterator)
        assertIteratorBackToBeginning(referenceIterator, testIterator)

        // add (at start)
        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()

        referenceIterator.add(1337L)
        testIterator.add(1337L)
        assertIteratorBackToBeginning(referenceIterator, testIterator)
        assertIteratorUntilEnd(referenceIterator, testIterator)

        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()

        referenceIterator.add(0L)
        testIterator.add(0L)
        assertTrue(testIterator.hasPrevious())
        assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious())
        val previousTestElement = testIterator.previous()
        assertEquals(0L, previousTestElement)
        assertEquals(referenceIterator.previous(), previousTestElement)
        assertIteratorUntilEnd(referenceIterator, testIterator)
        assertIteratorBackToBeginning(referenceIterator, testIterator)

        // remove after next
        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()

        // skip some
        for (i in 0..4) {
            referenceIterator.next()
            testIterator.next()
        }
        assertEquals(referenceIterator.next(), testIterator.next())
        referenceIterator.remove()
        testIterator.remove()
        assertIteratorUntilEnd(referenceIterator, testIterator)
        assertIteratorBackToBeginning(referenceIterator, testIterator)

        // remove after previous
        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()

        // skip some
        for (i in 0..4) {
            referenceIterator.next()
            testIterator.next()
        }
        assertEquals(referenceIterator.previous(), testIterator.previous())
        referenceIterator.remove()
        testIterator.remove()
        assertIteratorBackToBeginning(referenceIterator, testIterator)
        assertIteratorUntilEnd(referenceIterator, testIterator)
    }

    @Test
    fun testListIteratorSubsequentAdd() {
        var referenceIterator: MutableListIterator<Long> = referenceList.listIterator()
        var testIterator: MutableListIterator<Long> = testList.listIterator()

        // skip some
        for (i in 0..4) {
            referenceIterator.next()
            testIterator.next()
        }

        val additionalList: List<Long> = additionalList()
        additionalList.forEach {
            referenceIterator.add(it)
            testIterator.add(it)
        }

        assertWithReferenceList()

        // border case: beginning of list
        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()
        additionalList.forEach {
            referenceIterator.add(it)
            testIterator.add(it)
        }

        assertWithReferenceList()

    }

    private fun assertIteratorUntilEnd(referenceIterator: ListIterator<Long>, testIterator: ListIterator<Long>) {
        assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious())
        assertEquals(referenceIterator.hasNext(), testIterator.hasNext())
        var i = 0
        while (testIterator.hasNext()) {
            assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious(), "Index: $i")
            assertEquals(referenceIterator.hasNext(), testIterator.hasNext(), "Index: $i")

            assertEquals(referenceIterator.next(), testIterator.next(), "Index: $i")

            assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious(), "Index: $i")
            assertEquals(referenceIterator.hasNext(), testIterator.hasNext(), "Index: $i")
            i += 1
        }
    }

    private fun assertIteratorBackToBeginning(referenceIterator: ListIterator<Long>, testIterator: ListIterator<Long>) {
        assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious())
        assertEquals(referenceIterator.hasNext(), testIterator.hasNext())
        var i = referenceList.size - 1
        while (testIterator.hasPrevious()) {
            assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious(), "Index: $i")
            assertEquals(referenceIterator.hasNext(), testIterator.hasNext(), "Index: $i")

            assertEquals(referenceIterator.previous(), testIterator.previous(), "Index: $i")

            assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious(), "Index: $i")
            assertEquals(referenceIterator.hasNext(), testIterator.hasNext(), "Index: $i")
            i -= 1
        }
    }

    @Test
    fun testSublist() {
        var fromIndex = 1
        var toIndexExcl = 4

        // Normal sublist
        var referenceSublist: MutableList<Long> = referenceList.subList(fromIndex, toIndexExcl)
        var testSubList: MutableList<Long> = testList.subList(fromIndex, toIndexExcl)
        assertWithReferenceList(referenceSublist, testSubList)

        // change a value
        val newValue = 1337L
        val changeIndex = 1
        referenceSublist[changeIndex] = newValue
        testSubList[changeIndex] = newValue
        assertEquals(newValue, testSubList[changeIndex])
        assertWithReferenceList(referenceSublist, testSubList)
        // check if change was reflected in base list
        assertEquals(newValue, testList[fromIndex + changeIndex])
        assertWithReferenceList()

        // border cases
        // empty sublist at 0
        fromIndex = 0
        toIndexExcl = 0
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertTrue(testSubList.isEmpty())
        assertWithReferenceList(referenceSublist, testSubList)

        // empty sublist somewhere else
        fromIndex = 42
        toIndexExcl = 42
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertTrue(testSubList.isEmpty())
        assertWithReferenceList(referenceSublist, testSubList)

        // empty sublist at the end
        fromIndex = TEST_SIZE - 1
        toIndexExcl = TEST_SIZE - 1
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertTrue(testSubList.isEmpty())
        assertWithReferenceList(referenceSublist, testSubList)

        // assert one element sublist at the end (not out of bounds)
        fromIndex = TEST_SIZE - 1
        toIndexExcl = TEST_SIZE
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertEquals(1, testSubList.size)
        assertWithReferenceList(referenceSublist, testSubList)

        // sublist of whole list
        fromIndex = 0
        toIndexExcl = TEST_SIZE
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertEquals(testList.size, testSubList.size)
        assertWithReferenceList(referenceSublist, testSubList)

        // Test some error cases

        // start below 0
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList.subList(-1, 42) }

        // start beyond limit
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList.subList(TEST_SIZE, TEST_SIZE + 42) }

        // indexes swapped
        Assert.assertThrows(IllegalArgumentException::class.java) { testList.subList(1337, 42) }
    }

    @Test
    fun testSublistOfSublist() {
        val fromIndex = 42
        val toIndexExcl = 1337
        val referenceSubList: MutableList<Long> = referenceList.subList(fromIndex, toIndexExcl)
        val testSubList: MutableList<Long> = testList.subList(fromIndex, toIndexExcl)
        assertWithReferenceList(referenceSubList, testSubList)

        val fromIndexSub = 16
        val toIndexExclSub = 47
        val referenceSubSubList: MutableList<Long> = referenceSubList.subList(fromIndexSub, toIndexExclSub)
        val testSubSubList: MutableList<Long> = testSubList.subList(fromIndexSub, toIndexExclSub)
        assertWithReferenceList(referenceSubSubList, testSubSubList)

        val subSubIndex = 13
        val subIndex = fromIndexSub + subSubIndex
        val index = fromIndex + subIndex
        val newValue = 1337L

        testSubSubList[subSubIndex] = newValue
        assertEquals(newValue, testSubSubList[subSubIndex])
        assertEquals(newValue, testSubList[subIndex])
        assertEquals(newValue, testList[index])

        referenceSubSubList[subSubIndex] = newValue

        assertWithReferenceList(referenceSubSubList, testSubSubList)
        assertWithReferenceList(referenceSubList, testSubList)
        assertWithReferenceList()
    }

}