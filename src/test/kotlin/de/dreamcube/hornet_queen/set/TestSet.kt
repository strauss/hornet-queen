package de.dreamcube.hornet_queen.set

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import kotlin.random.Random

@RunWith(Parameterized::class)
class TestSet<T>(
    @Suppress("unused") // the test uses it for display
    private val testName: String,
    private val toTestSetConstructor: () -> MutableSet<T>,
    private val testValueSetConstructor: () -> MutableSet<T>,
    private val testValueGenerator: () -> T
) {
    private lateinit var testData: Set<T>
    private lateinit var negativeTestData: Set<T>
    private lateinit var toTest: MutableSet<T>

    companion object {
        private const val TEST_SIZE = 500_000

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> = arrayListOf(
            arrayOf("BitSet based 'Byte'", { PrimitiveByteSetB() }, { HashSet<Byte>() }, { Random.nextInt().toByte() }),
            arrayOf("BitSet based 'Short'", { PrimitiveShortSetB() }, { HashSet<Short>() },
                { Random.nextInt().toShort() }),
            arrayOf("BitSet based 'Char'", { PrimitiveCharSetB() }, { HashSet<Char>() },
                { Random.nextInt().toChar() }),
            arrayOf("BitSet based 'Int'", { PrimitiveIntSetB() }, { HashSet<Int>() }, { Random.nextInt() % TEST_SIZE }),
            arrayOf("HashTable based 'Byte'", { PrimitiveByteSet() }, { HashSet<Byte>() },
                { Random.nextInt().toByte() }),
            arrayOf("HashTable based 'Short'", { PrimitiveShortSet() }, { HashSet<Short>() },
                { Random.nextInt().toShort() }),
            arrayOf("HashTable based 'Char'", { PrimitiveCharSet() }, { HashSet<Int>() }, { Random.nextInt().toChar() }),
            arrayOf("HashTable based 'Int'", { PrimitiveIntSet() }, { HashSet<Int>() }, { Random.nextInt() }),
            arrayOf("HashTable based 'Long'", { PrimitiveLongSet() }, { HashSet<Long>() }, { Random.nextLong() }),
            arrayOf("HashTable based 'Float", { PrimitiveFloatSet() }, { HashSet<Float>() }, { Random.nextFloat() }),
            arrayOf("HashTable based 'Double", { PrimitiveDoubleSet() }, { HashSet<Double>() },
                { Random.nextDouble() }),
            arrayOf("HashTable based 'UUID", { UUIDSet() }, { HashSet<UUID>() }, { UUID.randomUUID() })
        )
    }


    @Before
    fun beforeEach() {
        toTest = toTestSetConstructor()
        val internalTestData: MutableSet<T> = testValueSetConstructor()
        val setSize = TEST_SIZE * 2

        // Create test data
        for (i in 0..<setSize) {
            internalTestData.add(testValueGenerator())
        }

        // remove half of the elements and add to negative set
        val negativeSetSize = internalTestData.size / 2
        val internalNegativeTestData = testValueSetConstructor()
        var i = 0
        val iterator = internalTestData.iterator()
        while (iterator.hasNext() && i < negativeSetSize) {
            val next = iterator.next()
            iterator.remove()
            internalNegativeTestData.add(next)
            i += 1
        }
        assert(internalTestData.size > 0 && internalNegativeTestData.size > 0)
        testData = internalTestData
        negativeTestData = internalNegativeTestData
    }

    @Test
    fun testAddSingle() {
        var expectedSize = 0
        Assert.assertEquals(expectedSize, toTest.size)
        Assert.assertTrue(toTest.isEmpty())

        // check if every element is contained after inserting
        for (current: T in testData) {
            toTest.add(current)
            expectedSize += 1
            Assert.assertTrue(toTest.contains(current))
            Assert.assertEquals(expectedSize, toTest.size)
        }

        // check if every element is contained after inserting all by single contains
        for (current: T in testData) {
            Assert.assertTrue(toTest.contains(current))
        }

        // check if every element is contained after inserting all by containsAll
        Assert.assertTrue(toTest.containsAll(testData))

        // check if every negative entry is not contained
        for (current in negativeTestData) {
            Assert.assertFalse(toTest.contains(current))
        }
    }

    @Test
    fun testAddAllAndClear() {
        // check if every element is contained after inserting all by contains all
        toTest.addAll(testData)
        Assert.assertTrue(toTest.containsAll(testData))

        // check if every negative entry is not contained
        for (current in negativeTestData) {
            Assert.assertFalse(toTest.contains(current))
        }

        // check size
        Assert.assertEquals(testData.size, toTest.size)

        toTest.clear()

        // check empty with size
        Assert.assertEquals(0, toTest.size)

        // check empty with empty()
        Assert.assertTrue(toTest.isEmpty())
    }

    @Test
    fun testRemove() {
        toTest.addAll(testData)

        val toRemove: Set<T> = extractSomeElements()

        var expectedSize = toTest.size
        for (current: T in toRemove) {
            Assert.assertEquals(expectedSize, toTest.size)
            toTest.remove(current)
            expectedSize -= 1
            Assert.assertFalse(toTest.contains(current))
        }

        Assert.assertEquals(expectedSize, toTest.size)

        // check that all deleted elements are no longer contained in the set
        for (current: T in toRemove) {
            Assert.assertFalse(toTest.contains(current))
        }
    }

    private fun extractSomeElements(): MutableSet<T> {
        val toRemove: MutableSet<T> = testValueSetConstructor()

        // the first five elements of the test data are subjects to be removed
        val iterator = testData.iterator()
        for (i in 0..5) {
            if (iterator.hasNext()) {
                val next = iterator.next()
                toRemove.add(next)
            }
        }
        return toRemove
    }

    @Test
    fun testRemoveAll() {
        toTest.addAll(testData)

        val toRemove: Set<T> = extractSomeElements()

        toTest.removeAll(toRemove)
        Assert.assertEquals(testData.size - toRemove.size, toTest.size)

        // check that all deleted elements are no longer contained in the set
        for (current: T in toRemove) {
            Assert.assertFalse(toTest.contains(current))
        }
    }

    @Test
    fun testRetainAll() {
        toTest.addAll(testData)
        val toRetain: Set<T> = extractSomeElements()
        val referenceSet = HashSet(testData) as MutableSet<T>
        referenceSet.removeAll(toRetain)

        toTest.retainAll(toRetain)

        Assert.assertEquals(toRetain.size, toTest.size)
        Assert.assertTrue(toTest.containsAll(toRetain))

        for (current in referenceSet) {
            Assert.assertFalse(toTest.contains(current))
        }

    }

    @Test
    fun testIterator() {
        toTest.addAll(testData)
        val iterator = toTest.iterator()
        var i = 0
        while (iterator.hasNext()) {
            // trying to f... around with hasNext() ... should be idempotent
            iterator.hasNext()
            iterator.hasNext()
            iterator.hasNext()
            val current = iterator.next()
            Assert.assertTrue(testData.contains(current))
            i += 1
        }

        // check if the iterator actually ran
        Assert.assertEquals(testData.size, i)
    }

    @Test
    fun testManualRehash() {
        val localToTest: MutableSet<T> = toTest
        if (localToTest is HashTableBasedSet<T>) {
            localToTest.addAll(testData)
            localToTest.manualRehash()

            // Test if all elements are still there after the manual rehash
            Assert.assertTrue(localToTest.containsAll(testData))
            Assert.assertEquals(testData.size, localToTest.size)
        }
    }

    @Test
    fun testShrinkToLoadFactor() {
        val localToTest: MutableSet<T> = toTest
        if (localToTest is HashTableBasedSet<T>) {
            localToTest.addAll(testData)
            localToTest.shrinkToLoadFactor()

            // Test if all elements are still there after the shrink operation
            Assert.assertTrue(localToTest.containsAll(testData))
            Assert.assertEquals(testData.size, localToTest.size)
        }
    }

}