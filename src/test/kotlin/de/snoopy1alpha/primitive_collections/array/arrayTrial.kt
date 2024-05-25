package de.snoopy1alpha.primitive_collections.array

import kotlin.random.Random

fun main() {
    val ia = PrimitiveIntArray(PrimitiveIntArray.MAX_SIZE / 2)
    val nia = PrimitiveIntArray(PrimitiveIntArray.MAX_SIZE / 2)
    for (i in 0..<ia.size) {
        val nextElement = Random.nextInt()
        ia[i] = nextElement
        nia[i] = nextElement
    }

    // Time measurements
    val expandBy = 250_000_000

    var start = System.currentTimeMillis()
    val iac = ia.getResizedCopy(expandBy)
    var stop = System.currentTimeMillis()
    println("Duration not native but arraycopy: ${stop - start} ms.")
    System.gc()

    Thread.sleep(1000L)

    start = System.currentTimeMillis()
    val niac = nia.getResizedCopy(expandBy)
    stop = System.currentTimeMillis()
    println("Duration native but not arraycopy: ${stop - start} ms.")

}