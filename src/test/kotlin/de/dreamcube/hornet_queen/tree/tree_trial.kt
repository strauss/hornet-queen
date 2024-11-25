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

    for (i in 5000..5010) {
        tree.insertAndPrint(i)
    }

    printTree(tree)

    tree.removeKey(50)
    tree.trimToSize()

    printTree(tree)

    println("Fini.")
}

private fun printTree(tree: PrimitiveTypeBinaryTree<Int>) {
    println(tree)
    println("Height: ${tree.height()}")
    println("Balanced: ${tree.isBalanced()}")
    println("Size: ${tree.size}")
}

private fun <K> PrimitiveTypeBinaryTree<K>.insertAndPrint(element: K) {
    insertKey(element)
    println(this)
}