package de.dreamcube.hornet_queen.map

import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.set.PrimitiveLongSet
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
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
        private const val TEST_SIZE = 100_000

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> = arrayListOf(
            arrayOf(
                "HashTable based 'Long' key and 'Int' value.",
                { HashTableBasedMapBuilder.useLongKey().useIntValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
            ),
            arrayOf(
                "HashTable based 'Long' key and 'Any' value.",
                { HashTableBasedMapBuilder.useLongKey().useArbitraryTypeValue<Any>().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { ArrayList<Any>() }, { Any() }
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


}