package de.dreamcube.hornet_queen.list

import de.dreamcube.hornet_queen.array.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(value = Parameterized::class)
class TestPrimitiveArrayBasedList<T>(
    private val name: String,
    private val testListConstructor: () -> PrimitiveArrayBasedList<T>,
    private val testDataConstructor: () -> PrimitiveArray<T>,
    private val testDataGenerator: () -> T,
    private val additionalList: () -> List<T>
) {

    private lateinit var testList: PrimitiveArrayBasedList<T>
    private lateinit var referenceList: ArrayList<T>
    private lateinit var testData: PrimitiveArray<T>
    private var actualTestSize: Int = 0

    companion object {
        private const val TEST_SIZE = 25000

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> = arrayListOf(
            arrayOf("Array list of 'Byte'", { PrimitiveByteArrayList() }, { PrimitiveByteArray(100) }, { Random.nextInt().toByte() },
                { listOf(System.currentTimeMillis().toByte(), 0.toByte(), 1.toByte(), 1337.toByte()) }),
            arrayOf("Linked list of 'Byte'", { PrimitiveByteLinkedList() }, { PrimitiveByteArray(100) }, { Random.nextInt().toByte() },
                { listOf(System.currentTimeMillis().toByte(), 0.toByte(), 1.toByte(), 1337.toByte()) }),
            arrayOf("Array list of 'Short'", { PrimitiveShortArrayList() }, { PrimitiveShortArray(TEST_SIZE) }, { Random.nextInt().toShort() },
                { listOf(System.currentTimeMillis().toShort(), 0.toShort(), 1.toShort(), 1337.toShort()) }),
            arrayOf("Linked list of 'Short'", { PrimitiveShortLinkedList() }, { PrimitiveShortArray(TEST_SIZE) }, { Random.nextInt().toShort() },
                { listOf(System.currentTimeMillis().toShort(), 0.toShort(), 1.toShort(), 1337.toShort()) }),
            arrayOf("Array list of 'Char'", { PrimitiveCharArrayList() }, { PrimitiveCharArray(TEST_SIZE) }, { Random.nextInt().toChar() },
                { listOf(System.currentTimeMillis().toInt().toChar(), 0.toChar(), 1.toChar(), 1337.toChar()) }),
            arrayOf("Linked list of 'Char'", { PrimitiveCharLinkedList() }, { PrimitiveCharArray(TEST_SIZE) }, { Random.nextInt().toChar() },
                { listOf(System.currentTimeMillis().toInt().toChar(), 0.toChar(), 1.toChar(), 1337.toChar()) }),
            arrayOf("Array list of 'Int'", { PrimitiveIntArrayList() }, { PrimitiveIntArray(TEST_SIZE) }, { Random.nextInt() },
                { listOf(System.currentTimeMillis().toInt(), 0, 1, 1337) }),
            arrayOf("Linked list of 'Int'", { PrimitiveIntLinkedList() }, { PrimitiveIntArray(TEST_SIZE) }, { Random.nextInt() },
                { listOf(System.currentTimeMillis().toInt(), 0, 1, 1337) }),
            arrayOf("Array list of 'Long'", { PrimitiveLongArrayList() }, { PrimitiveLongArray(TEST_SIZE) }, { Random.nextLong() },
                { listOf(System.currentTimeMillis(), 0L, 1L, 1337L) }),
            arrayOf("Linked list of 'Long'", { PrimitiveLongLinkedList() }, { PrimitiveLongArray(TEST_SIZE) }, { Random.nextLong() },
                { listOf(System.currentTimeMillis(), 0L, 1L, 1337L) }),
            arrayOf("Array list of 'Float'", { PrimitiveFloatArrayList() }, { PrimitiveFloatArray(TEST_SIZE) }, { Random.nextFloat() },
                { listOf(System.currentTimeMillis().toFloat(), 0.toFloat(), 1.toFloat(), 1337.toFloat()) }),
            arrayOf("Linked list of 'Float'", { PrimitiveFloatLinkedList() }, { PrimitiveFloatArray(TEST_SIZE) }, { Random.nextFloat() },
                { listOf(System.currentTimeMillis().toFloat(), 0.toFloat(), 1.toFloat(), 1337.toFloat()) }),
            arrayOf("Array list of 'Double'", { PrimitiveDoubleArrayList() }, { PrimitiveDoubleArray(TEST_SIZE) }, { Random.nextDouble() },
                { listOf(System.currentTimeMillis().toDouble(), 0.toDouble(), 1.toDouble(), 1337.toDouble()) }),
            arrayOf("Linked list of 'Double'", { PrimitiveDoubleLinkedList() }, { PrimitiveDoubleArray(TEST_SIZE) }, { Random.nextDouble() },
                { listOf(System.currentTimeMillis().toDouble(), 0.toDouble(), 1.toDouble(), 1337.toDouble()) }),
            arrayOf("Array list of 'UUID'", { UUIDArrayList() }, { UUIDArray(TEST_SIZE) }, { UUID.randomUUID() },
                {
                    listOf(
                        UUID(System.currentTimeMillis(), System.currentTimeMillis()), UUID(0L, 0L),
                        UUID(1L, 1L), UUID(1337L, 1337L)
                    )
                }),
            arrayOf("Linked list of 'UUID'", { UUIDLinkedList() }, { UUIDArray(TEST_SIZE) }, { UUID.randomUUID() },
                {
                    listOf(
                        UUID(System.currentTimeMillis(), System.currentTimeMillis()), UUID(0L, 0L),
                        UUID(1L, 1L), UUID(1337L, 1337L)
                    )
                })
        )
    }

    private fun generateTestData() {
        testData = testDataConstructor()
        testData.indices.forEach {
            val nextElement: T = testDataGenerator()
            testData[it] = nextElement
        }
    }

    @Before
    fun beforeEach() {
        generateTestData()
        // fill List and reference List with test data
        testList = testListConstructor()
        referenceList = ArrayList()
        for (element: T in testData) {
            testList.add(element)
            referenceList.add(element)
            assertEquals(referenceList.size, testList.size)
        }
        actualTestSize = referenceList.size
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

    private fun assertWithReferenceList(
        referenceList: List<T> = this.referenceList,
        testList: List<T> = this.testList
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
        val notContainedElement: T = randomNotContainedElement()

        assertFalse(testList.remove(notContainedElement))

        val containedElement: T = referenceList[randomIndex()]
        referenceList.remove(containedElement)
        assertTrue(testList.remove(containedElement))

        assertWithReferenceList()
    }

    @Test
    fun testRemoveAll() {
        val elements: List<T> = listOf(
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
        val elements: List<T> = listOf(
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

    private fun randomContainedElement(): T = referenceList[randomIndex()]

    private fun randomNotContainedElement(): T {
        var notContainedElement: T = testDataGenerator()
        while (referenceList.contains(notContainedElement)) {
            notContainedElement = testDataGenerator()
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
        val newValue: T = testDataGenerator()
        val testIndex: Int = randomIndex()

        var oldValueReference: T = referenceList.set(testIndex, newValue)
        var oldValue: T = testList.set(testIndex, newValue)

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
        assertNotEquals(actualTestSize, testList.getInternalArraySize())
        testList.trimToSize()
        assertEquals(actualTestSize, testList.getInternalArraySize())
        assertWithReferenceList(referenceList, testList)

        // Resize test
        val newElement: T = testDataGenerator()
        referenceList.add(newElement)
        testList.add(newElement)
        assertEquals(actualTestSize + (actualTestSize shr 1), testList.getInternalArraySize())
        assertWithReferenceList(referenceList, testList)
    }

    @Test
    fun testAddAtIndex() {
        val newValue: T = testDataGenerator()
        val randomIndex: Int = randomIndex()

        referenceList.add(randomIndex, newValue)
        testList.add(randomIndex, newValue)
        assertWithReferenceList()

        val lastIndex: Int = actualTestSize
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
        val additionalList: List<T> = additionalList()
        referenceList.addAll(additionalList)
        testList.addAll(additionalList)
        assertWithReferenceList()
    }

    @Test
    fun testAddAllAtIndex() {
        val additionalList: List<T> = additionalList()
        val randomIndex: Int = randomIndex()

        referenceList.addAll(randomIndex, additionalList)
        testList.addAll(randomIndex, additionalList)
        assertWithReferenceList()

        val lastIndex: Int = actualTestSize
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

        val newElement = testDataGenerator()

        referenceList.add(newElement)
        testList.add(newElement)
        assertWithReferenceList()
    }

    @Test
    fun testIndexFunctions() {
        val randomContainedElement: T = referenceList[randomIndex()]
        assertEquals(referenceList.indexOf(randomContainedElement), testList.indexOf(randomContainedElement))
        assertEquals(referenceList.lastIndexOf(randomContainedElement), testList.lastIndexOf(randomContainedElement))

        val randomNotContainedElement: T = randomNotContainedElement()
        assertEquals(referenceList.indexOf(randomNotContainedElement), testList.indexOf(randomNotContainedElement))
        assertEquals(
            referenceList.lastIndexOf(randomNotContainedElement), testList.lastIndexOf(randomNotContainedElement)
        )

        val firstElement: T = referenceList[0]
        assertEquals(referenceList.indexOf(firstElement), testList.indexOf(firstElement))
        assertEquals(referenceList.lastIndexOf(firstElement), testList.lastIndexOf(firstElement))

        val lastElement: T = referenceList[referenceList.size - 1]
        assertEquals(referenceList.indexOf(lastElement), testList.indexOf(lastElement))
        assertEquals(referenceList.lastIndexOf(lastElement), testList.lastIndexOf(lastElement))
    }

    @Test
    fun testListIterator() {
        // the usual
        var referenceIterator: MutableListIterator<T> = referenceList.listIterator()
        var testIterator: MutableListIterator<T> = testList.listIterator()
        assertIteratorUntilEnd(referenceIterator, testIterator)
        assertIteratorBackToBeginning(referenceIterator, testIterator)

        // add (at start)
        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()

        var newElement = testDataGenerator()

        referenceIterator.add(newElement)
        testIterator.add(newElement)
        assertIteratorBackToBeginning(referenceIterator, testIterator)
        assertIteratorUntilEnd(referenceIterator, testIterator)

        referenceIterator = referenceList.listIterator()
        testIterator = testList.listIterator()

        newElement = testDataGenerator()

        referenceIterator.add(newElement)
        testIterator.add(newElement)
        assertTrue(testIterator.hasPrevious())
        assertEquals(referenceIterator.hasPrevious(), testIterator.hasPrevious())
        val previousTestElement = testIterator.previous()
        assertEquals(newElement, previousTestElement)
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
        var referenceIterator: MutableListIterator<T> = referenceList.listIterator()
        var testIterator: MutableListIterator<T> = testList.listIterator()

        // skip some
        for (i in 0..4) {
            referenceIterator.next()
            testIterator.next()
        }

        val additionalList: List<T> = additionalList()
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

    private fun assertIteratorUntilEnd(referenceIterator: ListIterator<T>, testIterator: ListIterator<T>) {
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

    private fun assertIteratorBackToBeginning(referenceIterator: ListIterator<T>, testIterator: ListIterator<T>) {
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
        var referenceSublist: MutableList<T> = referenceList.subList(fromIndex, toIndexExcl)
        var testSubList: MutableList<T> = testList.subList(fromIndex, toIndexExcl)
        assertWithReferenceList(referenceSublist, testSubList)

        // change a value
        val newValue = testDataGenerator()
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
        fromIndex = actualTestSize - 1
        toIndexExcl = actualTestSize - 1
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertTrue(testSubList.isEmpty())
        assertWithReferenceList(referenceSublist, testSubList)

        // assert one element sublist at the end (not out of bounds)
        fromIndex = actualTestSize - 1
        toIndexExcl = actualTestSize
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertEquals(1, testSubList.size)
        assertWithReferenceList(referenceSublist, testSubList)

        // sublist of whole list
        fromIndex = 0
        toIndexExcl = actualTestSize
        referenceSublist = referenceList.subList(fromIndex, toIndexExcl)
        testSubList = testList.subList(fromIndex, toIndexExcl)
        assertEquals(testList.size, testSubList.size)
        assertWithReferenceList(referenceSublist, testSubList)

        // Test some error cases

        // start below 0
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList.subList(-1, 42) }

        // start beyond limit
        Assert.assertThrows(IndexOutOfBoundsException::class.java) { testList.subList(actualTestSize, actualTestSize + 42) }

        // indexes swapped
        Assert.assertThrows(IllegalArgumentException::class.java) { testList.subList(99, 42) }
    }

    @Test
    fun testSublistOfSublist() {
        val fromIndex = 42
        val toIndexExcl = 99
        val referenceSubList: MutableList<T> = referenceList.subList(fromIndex, toIndexExcl)
        val testSubList: MutableList<T> = testList.subList(fromIndex, toIndexExcl)
        assertWithReferenceList(referenceSubList, testSubList)

        val fromIndexSub = 16
        val toIndexExclSub = 47
        val referenceSubSubList: MutableList<T> = referenceSubList.subList(fromIndexSub, toIndexExclSub)
        val testSubSubList: MutableList<T> = testSubList.subList(fromIndexSub, toIndexExclSub)
        assertWithReferenceList(referenceSubSubList, testSubSubList)

        val subSubIndex = 13
        val subIndex = fromIndexSub + subSubIndex
        val index = fromIndex + subIndex
        val newValue = testDataGenerator()

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