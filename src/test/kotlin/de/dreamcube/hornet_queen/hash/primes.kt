package de.dreamcube.hornet_queen.hash

/**
 * <p>
 * This utility class was created as reaction for a YT video on an alternative prime determination algorithm by
 * Dijkstra. The video claimed that Dijkstra unites the best of two worlds (trial division and sieve of Erathostenes).
 * Here all three algorithms are implemented in a highly efficient variant for each of them (both speed and space wise).
 * By comparing the run times for high limits, it is shown, that the sieve implementation beats both in both speed and
 * space because of a space efficient BitSet based data structure for keeping track of the prime numbers.
 * </p>
 * <p>
 * Here the link to the YT video in question: https://www.youtube.com/watch?v=fwxjMKBMR7s (Dijkstra's hidden algorithm)
 * <\p>
 */

import java.math.BigInteger
import java.util.*
import kotlin.math.*

fun main() {
    val limit = 100_000_000
    performTrialDivision(limit)
    performPrimesBySieve(limit)
    performPrimesByDijkstra(limit)
}

/**
 * Required for determining an upper bound for the number of primes up to a certain limit. The constant is required for
 * computing the [li] function and ultimately the [bigLi] function that actually determines that upper bound.
 */
const val GAMMA: Double = 0.5772156649015329

/**
 * This is the logarithmic integral function approximated to a certain granularity. It can be used to compute [bigLi].
 * Here the lower bound is always 0 and the upper bound is [x]. The [liLimit] controls the granularity (iterations for
 * computing the series representation).
 */
private fun li(x: Double, liLimit: Int = 100): Double {
    val lnX = ln(x)
    var result: Double = GAMMA + ln(abs(lnX))

    for (k: Int in 1..liLimit) {
        val currentStep = lnX.pow(k.toDouble()) / (fac(k.toBigInteger()).toDouble() * k.toDouble())
        result += currentStep
    }
    return result
}

/**
 * <p>
 * According to the prime number theorem this function is an upper bound for the pi-function which is the number of
 * prime numbers up to a given limit.
 * </p>
 * <p>
 * We are jumping through all these hoops to optimize the space for the trial division approach. We could use the BitSet
 * structure that we use for the sieve algorithm but that one is slow when iterating through all primes found so far,
 * which we have to do very often in the trial division case.
 * </p>
 */
private fun bigLi(x: Double, liLimit: Int = 100): Double = li(x, liLimit) - li(2.0, liLimit)

private fun fac(bInt: BigInteger): BigInteger {
    if (bInt <= BigInteger.ONE) {
        return BigInteger.ONE
    }
    return bInt * fac(bInt.minus(BigInteger.ONE))
}

private fun performTrialDivision(limit: Int) {
    println("Trial Division")
    println("Limit: $limit")
    val start = System.currentTimeMillis()
    val result = primesByTrialDivision(limit)
    val stop = System.currentTimeMillis()
    if (limit < 1000) {
        val resultList = result.toList()
        println(resultList)
    }
    println("Number of primes found: ${result.numberOfPrimes()}")
    println("Highest prime: ${result.getHighest()}")

    println("Duration: ${stop - start} ms")
    println()
}

fun performPrimesBySieve(limit: Int) {
    println("Sieve of Eratosthenes")
    println("Limit: $limit")
    val start = System.currentTimeMillis()
    val result = primesBySieve(limit)
    val stop = System.currentTimeMillis()

    if (limit < 1000) {
        println(result.toList())
    }
    println("Number of primes found: ${result.numberOfPrimes()}")
    var highestPrime = 0
    val primeIterator = result.iterator()
    while (primeIterator.hasNext()) {
        highestPrime = primeIterator.next()
    }
    println("Highest prime: $highestPrime")
    println("Duration: ${stop - start} ms")
    println()
}

fun performPrimesByDijkstra(limit: Int) {
    println("Dijkstra's dirty little secret")
    println("Limit: $limit")
    val start = System.currentTimeMillis()
    val result = primesByDijkstra(limit)
    val stop = System.currentTimeMillis()

    if (limit < 1000) {
        println(result)
    }
    println("Number of primes found: ${result.numberOfPrimes()}")
    println("Highest prime: ${result.getHighest()}")

    println("Duration: ${stop - start} ms")

}

fun primesByDijkstra(limit: Int = Int.MAX_VALUE): DijkstraResultStructure {
    if (limit < 2) {
        return DijkstraResultStructure(0, limit)
    }

    // approximation of maximum number of primes in interval very near to pi function
    val arraySize: Int = ceil(bigLi(limit.toDouble())).toInt()
    println("Array size: $arraySize ... memory approx: ${arraySize * 4} bytes")

    val primesFound = DijkstraResultStructure(arraySize, limit)
    primesFound.add(2)

    // let the magic begin
    for (currentToTest: Int in 3..limit) {
        val smallestMultpile: Int = primesFound.getSmallestMultiple()
        if (currentToTest < smallestMultpile) {
            // prime
            primesFound.add(currentToTest)
        } else {
            assert(currentToTest == smallestMultpile)
            // not prime
            primesFound.increaseSmallestMultiple()
        }
        showProgress(currentToTest)
    }
    println()

    // Object overhead 16 bytes + two fields in object à 4 bytes (int)
    println("Elements in PQueue: ${primesFound.getRelevantMultiplesSize()} - Additional memory consumption approx: ${primesFound.getRelevantMultiplesSize() * 24} bytes")

    return primesFound
}

/**
 * Implementation of "Sieve of Erathostenes" using a space efficient [BitSetResultStructure].
 */
fun primesBySieve(limit: Int = Int.MAX_VALUE): BitSetResultStructure {
    if (limit < 2) {
        return BitSetResultStructure(0)
    }
    // the result contains all numbers that are not prime
    val primesFound = BitSetResultStructure(limit)

    // zero and one are not prime
    primesFound.setNotPrime(0)
    primesFound.setNotPrime(1)

    setMultiplesNotPrime(2, limit, primesFound)
    val calcLimit: Int = ceil(sqrt(limit.toDouble())).toInt()
    for (currentNumber: Int in 2..calcLimit) {
        if (primesFound.isPrime(currentNumber)) {
            setMultiplesNotPrime(currentNumber, limit, primesFound)
        }
    }

    return primesFound
}

private fun setMultiplesNotPrime(currentPrime: Int, limit: Int, primesFound: BitSetResultStructure) {
    // we have to compute in Long because of Integer overflow at high limits
    var currentPosition: Long = currentPrime.toLong() + currentPrime.toLong()
    val limitAsLong = limit.toLong()
    val currentPrimeAsLong = currentPrime.toLong()
    while (currentPosition <= limitAsLong) {
        primesFound.setNotPrime(currentPosition.toInt())
        currentPosition += currentPrimeAsLong
    }
}

fun primesByTrialDivision(limit: Int = Int.MAX_VALUE): ResultStructure {
    if (limit < 2) {
        return ResultStructure(0)
    }
    // approximation of maximum number of primes in interval very near to pi function
    val arraySize: Int = ceil(bigLi(limit.toDouble())).toInt()
    println("Array size: $arraySize ... memory approx: ${arraySize * 4} bytes")

    val primesFound = ResultStructure(arraySize)
    primesFound.add(2)

    for (currentToTest: Int in 3..limit step 2) {
        var currentIsPrime = true
        val currentToTestSqrt: Int = ceil(sqrt(currentToTest.toDouble())).toInt()
        val iterator: Iterator<Int> = primesFound.iterator()
        iterator.next() // skip 2
        while (iterator.hasNext()) {
            val currentPrime: Int = iterator.next()
            if (currentPrime > currentToTestSqrt) {
                break
            }
            if (currentToTest % currentPrime == 0) {
                currentIsPrime = false
                break
            }
        }
        if (currentIsPrime) {
            primesFound.add(currentToTest)
        }
        showProgress(currentToTest)
    }
    println()

    return primesFound
}

private fun showProgress(currentToTest: Int) {
    if (currentToTest % 1000000 == 1) {
        print('.')
    }
    if (currentToTest % 100000000 == 1) {
        println()
    }
}

/**
 * Wrapper class for a [BitSet]. The prime numbers are identified by their index and the "false" flag. Initially the
 * sieve algorithm assumes all numbers as prime. By inverting the semantic we save this initialization step. For a limit
 * of [Int.MAX_VALUE] the structure uses ~260MB of heap space. Each number only consumes 1 Bit, but all numbers in the
 * range are present. The alternative structure would be an array holding all primes (see [ResultStructure]). The
 * amount of numbers is significantly lower but each number consumes 32 bits/4 bytes.
 */
class BitSetResultStructure(private val limit: Int = Int.MAX_VALUE) : Iterable<Int> {
    private val internalStructure = BitSet(limit)

    fun numberOfPrimes(): Int = limit - internalStructure.cardinality() + 1

    fun setNotPrime(number: Int) {
        internalStructure.set(number)
    }

    fun isPrime(number: Int): Boolean {
        return !internalStructure.get(number)
    }

    fun nextPrime(number: Int): Int {
        return internalStructure.nextClearBit(number)
    }

    /**
     * Spits out all found prime numbers in ascending order
     */
    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        val limitAsLong = limit.toLong()
        var currentPosition: Long = 2L

        override fun hasNext(): Boolean = currentPosition <= limitAsLong && isPrime(currentPosition.toInt())

        override fun next(): Int {
            if (!hasNext()) {
                throw NoSuchElementException()
            }

            // hold back result
            val result = currentPosition

            // compute next result
            currentPosition += 1
            if (currentPosition <= limitAsLong) {
                currentPosition = internalStructure.nextClearBit(currentPosition.toInt()).toLong()
            }

            // spit out result
            return result.toInt()
        }
    }

}

class ResultStructure(maxSize: Int = Int.MAX_VALUE - 8) : Iterable<Int> {
    var currentSize = 0
        private set

    private val theArray = IntArray(maxSize)

    fun add(value: Int) {
        theArray[currentSize] = value
        currentSize += 1
    }

    fun numberOfPrimes(): Int = currentSize

    operator fun get(index: Int) = theArray[index]

    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        var currentPosition: Int = 0

        override fun hasNext(): Boolean = currentPosition < currentSize

        override fun next(): Int {
            val result: Int = theArray[currentPosition]
            currentPosition += 1
            return result
        }

    }

    fun getHighest(): Int = theArray[currentSize - 1]

    override fun toString(): String {
        val result = StringBuilder()
        result.append('[')
        for (current: Int in this) {
            result.append(current)
            result.append(", ")
        }
        if (this.currentSize > 0) {
            result.setLength(result.length - 2)
        }
        result.append(']')
        return result.toString()
    }

}

class DijkstraResultStructure(maxSize: Int = Int.MAX_VALUE - 8, limit: Int) : Iterable<Int> {

    private val primeLimit: Int = ceil(sqrt(limit.toDouble())).toInt()
    private val primesFound = ResultStructure(maxSize)
    private val relevantMultiples = PriorityQueue<RelevantMultiple>()

    // working list, on this level for avoiding too much GC
    private val smallestRelevantMultiples = ArrayList<RelevantMultiple>()

    fun add(prime: Int) {
        primesFound.add(prime)
        if (prime <= primeLimit) {
            val multiple: Int = prime * prime
            relevantMultiples.offer(RelevantMultiple(prime, multiple))
        }
    }

    fun numberOfPrimes(): Int = primesFound.numberOfPrimes()

    fun getSmallestMultiple(): Int = relevantMultiples.peek().multiple

    fun increaseSmallestMultiple() {
        val smallestMultiple = getSmallestMultiple()
        while (relevantMultiples.isNotEmpty() && getSmallestMultiple() == smallestMultiple) {
            smallestRelevantMultiples.add(relevantMultiples.remove())
        }
        smallestRelevantMultiples.forEach { currentRelevantMultiple: RelevantMultiple ->
            currentRelevantMultiple.increase()
            relevantMultiples.offer(currentRelevantMultiple)
        }
        smallestRelevantMultiples.clear()
    }

    fun getRelevantMultiplesSize(): Int = relevantMultiples.size

    override fun iterator(): Iterator<Int> = primesFound.iterator()

    fun getHighest(): Int = primesFound.getHighest()

    override fun toString(): String = primesFound.toString()

    class RelevantMultiple(private val prime: Int, var multiple: Int) : Comparable<RelevantMultiple> {
        fun increase() {
            multiple += prime
        }

        override operator fun compareTo(other: RelevantMultiple): Int = multiple.compareTo(other.multiple)

        override fun toString(): String = "($prime:$multiple)"
    }

}