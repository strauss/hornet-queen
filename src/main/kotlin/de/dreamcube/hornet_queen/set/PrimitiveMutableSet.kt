/*
 * Hornet Queen
 * Copyright (c) 2024 Sascha Strauß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dreamcube.hornet_queen.set

interface PrimitiveMutableSet<T> : MutableSet<T> {

    override fun addAll(elements: Collection<T>): Boolean {
        var result = false
        elements.forEach {
            result = add(it) || result
        }
        return result
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        elements.forEach {
            if (!contains(it)) {
                return false
            }
        }
        return true
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var result = false
        val iterator = iterator()
        while (iterator.hasNext()) {
            val current: T = iterator.next()
            if (!elements.contains(current)) {
                iterator.remove()
                result = true
            }
        }
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var result = false
        elements.forEach {
            result = remove(it) || result
        }
        return result
    }

    fun asString(): String {
        val result = StringBuilder()
        result.append('{')
        result.append(asSequence().joinToString(", "))
        result.append('}')
        return result.toString()
    }

}