package de.snoopy1alpha.primitive_collections.array

import kotlin.random.Random

fun main() {
    val elements = 250_000_000
    println("Elements: $elements")
    println()
    println("LongArray")
    var start = System.currentTimeMillis()
    val ia = LongArray(elements)
    for (i in 0..<elements) {
        ia[i] = Random.nextInt().toLong()
    }
    var x = 0L
    for (i: Long in ia) {
        x += i
    }
    var stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()
    println("PrimitiveArrayWithConverters")
    start = System.currentTimeMillis()
    val pa: PrimitiveArrayWithConverters<Long> = PrimitiveArrayWithConverters(
        elements,
        8,
        PrimitiveArrayConverters::longOutConverter,
        PrimitiveArrayConverters::longInConverter
    )
    for (i in 0..<elements) {
        pa[i] = Random.nextInt().toLong()
    }
    x = 0
    for (d: Long in pa) {
        x += d
    }
    stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()
    println("PrimitiveLongArray")
    start = System.currentTimeMillis()
    val pia = PrimitiveLongArray(elements)
    for (i in 0..<elements) {
        pia[i] = Random.nextInt().toLong()
    }
    x = 0
    for (d: Long in pia) {
        x += d
    }
    stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()

    println("PrimitiveLongArray (direct allocation)")
    start = System.currentTimeMillis()
    val ppia = PrimitiveLongArray(elements)
    for (i in 0..<elements) {
        ppia[i] = Random.nextInt().toLong()
    }
    x = 0
    for (d: Long in ppia) {
        x += d
    }
    stop = System.currentTimeMillis()
    println("Sum of random values: $x")
    println("Duration: ${stop - start} ms")
    println()
}