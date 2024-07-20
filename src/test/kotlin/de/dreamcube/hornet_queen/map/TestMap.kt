package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.list.*
import de.dreamcube.hornet_queen.set.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import kotlin.random.Random

@RunWith(Parameterized::class)
class TestMap<K, V>(
    private val testName: String,
    private val toTestMapConstructor: () -> MutableMap<K, V>,
    private val testKeySetConstructor: () -> MutableSet<K>,
    private val testKeyGenerator: () -> K,
    private val testValueListConstructor: () -> MutableList<V>,
    private val testValueGenerator: () -> V
) {
    private lateinit var testKeyData: Set<K>
    private lateinit var negativeTestKeyData: Set<K>
    private lateinit var testValueData: List<V>
    private lateinit var toTest: MutableMap<K, V>

    companion object {
        private const val TEST_SIZE = 10_000

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> = arrayListOf(
            arrayOf(
                "HashTable based 'Long' key and 'Byte' value.",
                { HashTableBasedMapBuilder.useLongKey().useByteValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveByteArrayList() }, { Random.nextInt().toByte() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Short' value.",
                { HashTableBasedMapBuilder.useLongKey().useShortValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveShortArrayList() }, { Random.nextInt().toShort() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Int' value.",
                { HashTableBasedMapBuilder.useLongKey().useIntValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Long' value.",
                { HashTableBasedMapBuilder.useLongKey().useLongValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveLongArrayList() }, { Random.nextLong() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Float' value.",
                { HashTableBasedMapBuilder.useLongKey().useFloatValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveFloatArrayList() }, { Random.nextFloat() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Double' value.",
                { HashTableBasedMapBuilder.useLongKey().useDoubleValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveDoubleArrayList() }, { Random.nextDouble() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'UUID' value.",
                { HashTableBasedMapBuilder.useLongKey().useUUIDValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { UUIDArrayList() }, { UUID.randomUUID() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Any' value.",
                { HashTableBasedMapBuilder.useLongKey().useArbitraryTypeValue<Any>().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { ArrayList<Any>() }, { Any() }
            ),
            arrayOf(
                "HashTable based 'Byte' key and 'Int' value.",
                { HashTableBasedMapBuilder.useByteKey().useIntValue().create() }, { PrimitiveByteSetB() },
                { Random.nextInt().toByte() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'Short' key and 'Int' value.",
                { HashTableBasedMapBuilder.useShortKey().useIntValue().create() }, { PrimitiveShortSetB() },
                { Random.nextInt().toShort() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'Int' key and 'Int' value.",
                { HashTableBasedMapBuilder.useIntKey().useIntValue().create() }, { PrimitiveIntSet() },
                { Random.nextInt() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'Float' key and 'Int' value.",
                { HashTableBasedMapBuilder.useFloatKey().useIntValue().create() }, { PrimitiveFloatSet() },
                { Random.nextFloat() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'Double' key and 'Int' value.",
                { HashTableBasedMapBuilder.useDoubleKey().useIntValue().create() }, { PrimitiveDoubleSet() },
                { Random.nextInt().toDouble() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'UUID' key and 'Int' value.",
                { HashTableBasedMapBuilder.useUUIDKey().useIntValue().create() }, { UUIDSet() },
                { UUID.randomUUID() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            )
        )
    }

    @Before
    fun beforeEach() {
        toTest = toTestMapConstructor()
        val internalTestKeyData: MutableSet<K> = testKeySetConstructor()
        val keySetSize = TEST_SIZE * 2

        // Create test key data
        for (i in 0..<keySetSize) {
            internalTestKeyData.add(testKeyGenerator())
        }

        // remove half of the elements and add to negative key set
        val negativeKeySetSize = internalTestKeyData.size / 2
        val internalNegativeTestKeyData = testKeySetConstructor()
        var i = 0
        val iterator = internalTestKeyData.iterator()
        while (iterator.hasNext() && i < negativeKeySetSize) {
            val next = iterator.next()
            iterator.remove()
            internalNegativeTestKeyData.add(next)
            i += 1
        }
        assert(internalTestKeyData.size > 0 && internalNegativeTestKeyData.size > 0)
        testKeyData = internalTestKeyData
        negativeTestKeyData = internalNegativeTestKeyData

        // Create test value data
        val internalTestValueData = testValueListConstructor()
        for (k in testKeyData.indices) {
            internalTestValueData.add(testValueGenerator())
        }
        testValueData = internalTestValueData
    }

    @Test
    fun testPutSingle() {
        var expectedSize = 0
        Assert.assertEquals(expectedSize, toTest.size)
        Assert.assertTrue(toTest.isEmpty())

        // check if every key is mapped after inserting
        for (currentKey: K in testKeyData) {
            val currentValue = testValueData[expectedSize]
            val oldValue: V? = toTest.put(currentKey, currentValue)
            Assert.assertNull(oldValue)
            expectedSize += 1
            Assert.assertTrue(
                "Recently added key '$currentKey' was not found in map. Current size is '${toTest.size}', expected size is '$expectedSize'.",
                toTest.containsKey(currentKey)
            )
            Assert.assertEquals(expectedSize, toTest.size)
            Assert.assertEquals(currentValue, toTest[currentKey])
        }

        // check if every key is contained after inserting all by single containsKey
        // also check if all values match
        var i = 0
        for (currentKey: K in testKeyData) {
            Assert.assertTrue(toTest.containsKey(currentKey))
            val currentValue = testValueData[i]
            Assert.assertEquals(testValueData[i], currentValue)
            i += 1
        }

        // check if all values are contained
        for (currentValue: V in testValueData) {
            Assert.assertTrue(toTest.containsValue(currentValue))
        }

        // check if all non-keys are not contained
        for (currentKey: K in negativeTestKeyData) {
            Assert.assertFalse(toTest.containsKey(currentKey))
        }
    }

    @Test
    fun testPutAllAndClear() {
        // first create a reference map with the test data
        val referenceMap: MutableMap<K, V> = HashMap()
        fillWithTestData(referenceMap)

        // add all values of reference map to test map and test if they are all included
        toTest.putAll(referenceMap)
        Assert.assertEquals(referenceMap.size, toTest.size)
        Assert.assertTrue(toTest.keys.containsAll(referenceMap.keys))
        Assert.assertTrue(toTest.values.containsAll(referenceMap.values))

        // check all mappings individually
        for (currentEntry: Map.Entry<K, V> in referenceMap.entries) {
            Assert.assertEquals(referenceMap[currentEntry.key], toTest[currentEntry.key])
        }

        // check if every negative key entry is not contained
        for (current in negativeTestKeyData) {
            Assert.assertFalse(toTest.containsKey(current))
        }

        // test if clear actually clears
        toTest.clear()
        Assert.assertEquals(0, toTest.size)
        Assert.assertTrue(toTest.isEmpty())
        Assert.assertFalse(toTest.isNotEmpty())
    }

    private fun fillWithTestData(referenceMap: MutableMap<K, V>) {
        var i = 0
        for (currentKey: K in testKeyData) {
            referenceMap[currentKey] = testValueData[i]
            i += 1
        }
    }

    @Test
    fun testRemove() {
        fillWithTestData(toTest)

        val toRemove: Set<K> = extractSomeKeys()

        var expectedSize = toTest.size
        for (current: K in toRemove) {
            // check size before (and after every remove)
            Assert.assertEquals(expectedSize, toTest.size)
            toTest.remove(current)
            expectedSize -= 1
            // check if size shrank
            Assert.assertFalse(toTest.containsKey(current))
        }

        // check size after last remove
        Assert.assertEquals(expectedSize, toTest.size)

        // check that all deleted keys are no longer contained in the map
        for (current: K in toRemove) {
            Assert.assertFalse(toTest.containsKey(current))
        }
    }

    private fun extractSomeKeys(): MutableSet<K> {
        val toRemove: MutableSet<K> = testKeySetConstructor()

        // the first five elements of the test data are subjects to be removed
        val iterator = testKeyData.iterator()
        for (i in 0..5) {
            if (iterator.hasNext()) {
                val next = iterator.next()
                toRemove.add(next)
            }
        }
        return toRemove
    }

    @Test
    fun testEntryIterator() {
        fillWithTestData(toTest)
        val iterator = toTest.iterator()
        var i = 0
        while (iterator.hasNext()) {
            // trying to f... around with hasNext() ... should be itempotent
            iterator.hasNext()
            iterator.hasNext()
            iterator.hasNext()
            val currentEntry = iterator.next()
            Assert.assertTrue(testKeyData.contains(currentEntry.key))
            Assert.assertTrue(testValueData.contains(currentEntry.value))
            i += 1
        }

        // check if the iterator actually ran
        Assert.assertEquals(testKeyData.size, i)
    }

    @Test
    fun testKeyIterator() {
        fillWithTestData(toTest)
        val iterator = toTest.keys.iterator()
        var i = 0
        while (iterator.hasNext()) {
            // trying to f... around with hasNext() ... should be itempotent
            iterator.hasNext()
            iterator.hasNext()
            iterator.hasNext()
            val currentKey = iterator.next()
            Assert.assertTrue(testKeyData.contains(currentKey))
            i += 1
        }

        // check if the iterator actually ran
        Assert.assertEquals(testKeyData.size, i)
    }

    @Test
    fun testValueIterator() {
        fillWithTestData(toTest)
        val values = toTest.values

        // check if the values collection has the same size as the map itself
        Assert.assertEquals(toTest.size, values.size)

        val iterator = values.iterator()
        var i = 0
        while (iterator.hasNext()) {
            // trying to f... around with hasNext() ... should be itempotent
            iterator.hasNext()
            iterator.hasNext()
            iterator.hasNext()
            val currentValue: V = iterator.next()
            Assert.assertTrue("At position '$i' value '$currentValue' is not included in the value collection.", testValueData.contains(currentValue))
            i += 1
        }

        // check if the iterator actually ran
        Assert.assertEquals(testValueData.size, i)
    }

    @Test
    fun testManualRehash() {
        val localToTest: MutableMap<K, V> = toTest
        if (localToTest is HashTableBasedMap<K, V>) {
            fillWithTestData(localToTest)
            localToTest.manualRehash()

            // Test if all elements are still there after the manual rehash
            Assert.assertTrue(localToTest.keys.containsAll(testKeyData))
            Assert.assertTrue(localToTest.values.containsAll(testValueData))
            Assert.assertEquals(testKeyData.size, localToTest.size)
        }
    }

    @Test
    fun testShrinkToLoadFactor() {
        val localToTest: MutableMap<K, V> = toTest
        if (localToTest is HashTableBasedMap<K, V>) {
            fillWithTestData(localToTest)
            localToTest.shrinkToLoadFactor()

            // Test if all elements are still there after the shrink operation
            Assert.assertTrue(localToTest.keys.containsAll(testKeyData))
            Assert.assertTrue(localToTest.values.containsAll(testValueData))
            Assert.assertEquals(testKeyData.size, localToTest.size)
        }
    }
}