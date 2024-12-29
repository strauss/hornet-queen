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

package de.dreamcube.hornet_queen.tree

fun main() {
    val tree: PrimitiveTypeBinaryTree<Int> = PrimitiveIntBinaryTree()
//    originalTrial(tree)

//    val testdata = listOf(-1000, 100, 70, 50, 74, 4711, 1, 75, 42, 1337, 25, 2, 32, -16, 16, -1, 150, -32)
    val testdata = listOf(100, 50, 150, 25, 1, -1, 2, 75, 1337, 42, 16, 70, 4711, 74, -1000, 32, -32, -16)
    for (t in testdata) {
        tree.insertAndPrint(t)
    }
    tree.removeKey(-1000)
    println(tree)
    tree.removeKey(100)
    println(tree)
    tree.printAllHeights()

    println("Fini.")
}

private fun <K> PrimitiveTypeBinaryTree<K>.printAllHeights() {
    for (i in inorderIndexIterator()) {
        printHeight(i)
    }
}

private fun <K> PrimitiveTypeBinaryTree<K>.printHeight(index: Int) {
    println("Index: $index - Key: ${keys[index]} - HeightR: ${this.heightR(index)} - HeightI: ${this.heightI(index)} - Height: ${height[index]}")
}

private fun originalTrial(tree: PrimitiveTypeBinaryTree<Int>) {
    tree.insertAndPrint(100)

    tree.insertAndPrint(50)
    tree.insertAndPrint(150)
    tree.insertAndPrint(25)
    tree.insertAndPrint(1)
    tree.insertAndPrint(-1)
    tree.insertAndPrint(2)
    tree.insertAndPrint(75)
    tree.insertAndPrint(1337)
    tree.insertAndPrint(42)
    tree.insertAndPrint(16)
    tree.insertAndPrint(70)
    tree.insertAndPrint(4711)
    tree.insertAndPrint(74)
    tree.insertAndPrint(-1000)
    tree.insertAndPrint(32)
    tree.insertAndPrint(-32)
    tree.insertAndPrint(-16)

    var indexIterator = tree.unorderedIndexIterator()
    println()
    println("Iterator")
    while (indexIterator.hasNext()) {
        println(tree.keys[indexIterator.next()])
    }

    for (i in 5000..5010) {
        tree.insertAndPrint(i)
    }

    printTree(tree)

    tree.removeKey(50)
    tree.trimToSize()

    printTree(tree)
    var sustained = 0
    indexIterator = tree.inorderIndexIterator()
    while (indexIterator.hasNext()) {
        val current: Int = indexIterator.next()
        val element = tree.keys[current]
        if (element % 2 == 1) {
            indexIterator.remove()
        } else {
            sustained += 1
        }
    }
    println("Sustained: $sustained")
    printTree(tree)
}

private fun printTree(tree: PrimitiveTypeBinaryTree<Int>) {
    println(tree)
//    println(tree.toStringR())
    println("Height: ${tree.height()}")
    println("Balanced: ${tree.isBalanced()}")
    println("Size: ${tree.size}")
}

private fun <K> PrimitiveTypeBinaryTree<K>.insertAndPrint(element: K) {
    insertKey(element)
    println(this)
}