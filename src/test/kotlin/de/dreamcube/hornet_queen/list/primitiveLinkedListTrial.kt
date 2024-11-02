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

package de.dreamcube.hornet_queen.list

fun main() {
    val thaList = PrimitiveLongLinkedList(7)
    thaList.add(0L)
    thaList.add(1L)
    thaList.add(2L)
    thaList.add(3L)
    thaList.add(4L)
//    val listIterator = thaList.listIterator()
//    listIterator.next() // skip 0
//    listIterator.next() // skip 1
//    listIterator.add(1337L)
//    listIterator.add(4711L)
//    thaList.add(3, 4177L)

    iterate(thaList)


    println("Fini.")
}

private fun iterate(thaList: PrimitiveArrayBasedList<Long>) {
    val iterator = thaList.listIterator()
    while (iterator.hasNext()) {
        println(iterator.next())
    }
    println("...")
    while (iterator.hasPrevious()) {
        println(iterator.previous())
    }
}