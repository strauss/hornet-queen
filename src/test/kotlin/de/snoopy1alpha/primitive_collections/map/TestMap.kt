package de.snoopy1alpha.primitive_collections.map

import de.snoopy1alpha.primitive_collections.list.PrimitiveIntArrayList
import de.snoopy1alpha.primitive_collections.set.PrimitiveLongSet
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
        private const val TEST_SIZE = 500_000

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Iterable<Array<Any>> = arrayListOf(
            arrayOf(
                "HashTable based 'Long' key and 'Int' value.",
                { HashTableBasedMapBuilder.useLongKey().useIntValue().create() }, { PrimitiveLongSet() },
                { Random.nextLong() }, { PrimitiveIntArrayList() }, { Random.nextInt() }
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
            Assert.assertTrue(toTest.containsKey(currentKey))
            Assert.assertEquals(expectedSize, toTest.size)
            Assert.assertEquals(currentValue, toTest[currentKey])
        }
    }


}