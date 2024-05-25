package de.snoopy1alpha.primitive_collections.array

import de.snoopy1alpha.primitive_collections.*

object PrimitiveArrayConverters {
    fun byteOutConverter(elements: ByteArray): Byte {
        assert(elements.size == BYTE_SIZE)
        return elements[0]
    }

    fun byteInConverter(element: Byte): ByteArray = byteArrayOf(element)

    fun intOutConverter(elements: ByteArray): Int {
        assert(elements.size == INT_SIZE)
        var result = 0x00
        for (i in INT_SIZE - 1 downTo 0) {
            result = result shl BYTE_BITS
            val current: Int = 0xff and elements[i].toInt()
            result = result or current
        }
        return result
    }

    fun intInConverter(element: Int): ByteArray {
        val result = ByteArray(INT_SIZE)
        for (i in 0..<INT_SIZE) {
            val currentShift = BYTE_BITS * i
            val currentMask: Int = 0xff shl currentShift
            val currentValue: Int = (element and currentMask) ushr currentShift
            result[i] = currentValue.toByte()

        }
        return result
    }

    fun longOutConverter(elements: ByteArray): Long {
        assert(elements.size == LONG_SIZE)
        var result = 0x00L
        for (i in LONG_SIZE - 1 downTo 0) {
            result = result shl BYTE_BITS
            val current: Long = 0xffL and elements[i].toLong()
            result = result or current
        }
        return result
    }

    fun longInConverter(element: Long): ByteArray {
        val result = ByteArray(LONG_SIZE)
        for (i in 0..<LONG_SIZE) {
            val currentShift = BYTE_BITS * i
            val currentMask: Long = 0xffL shl currentShift
            val currentValue: Long = (element and currentMask) ushr currentShift
            result[i] = currentValue.toByte()
        }
        return result
    }

    fun shortOutConverter(elements: ByteArray): Short {
        assert(elements.size == SHORT_SIZE)
        // We have to work with Int because for some reason the bit operators are not defined for short values
        var result = 0
        for (i in SHORT_SIZE - 1 downTo 0) {
            result = result shl BYTE_BITS
            val current: Int = 0xff and elements[i].toInt()
            result = result or current
        }
        return result.toShort()
    }

    fun shortInConverter(element: Short): ByteArray {
        val result = ByteArray(SHORT_SIZE)
        for (i in 0..<SHORT_SIZE) {
            val currentShift = BYTE_BITS * i
            val currentMask: Int = 0xff shl currentShift
            // The signed cast to int is not a problem because the bitwise "and" with the mask kills all sign bits
            val currentValue: Int = (element.toInt() and currentMask) ushr currentShift
            result[i] = currentValue.toByte()
        }
        return result
    }

    fun floatOutConverter(elements: ByteArray): Float {
        assert(elements.size == FLOAT_SIZE)
        val result: Int = intOutConverter(elements)
        return Float.fromBits(result)
    }

    fun floatInConverter(element: Float): ByteArray {
        val floatBits: Int = element.toRawBits()
        return intInConverter(floatBits)
    }

    fun doubleOutConverter(elements: ByteArray): Double {
        assert(elements.size == DOUBLE_SIZE)
        val result: Long = longOutConverter(elements)
        return Double.fromBits(result)
    }

    fun doubleInConverter(element: Double): ByteArray {
        val doubleBits: Long = element.toRawBits()
        return longInConverter(doubleBits)
    }
}