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

import de.dreamcube.hornet_queen.NO_INDEX
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import kotlin.math.abs
import kotlin.math.max

/**
 * Tree traversal with visitor pattern.
 */
fun <K, V> PrimitiveTypeBinaryTree<K, V>.traverseTree(index: Int, visitor: TreeVisitor<K>) {
    if (index == NO_INDEX) {
        return
    }

    val stack = PrimitiveIntArrayList()
    val visitedLeft = PrimitiveIntSetB()
    val visitedRight = PrimitiveIntSetB()
    val nodeProcessed = PrimitiveIntSetB()

    stack.add(index)
    while (stack.isNotEmpty()) {
        val currentIndex = stack[stack.lastIndex]
        val currentElement = keys[currentIndex]

        if (!visitedLeft.contains(currentIndex) && !visitedRight.contains(currentIndex)) {
            visitor.enterNode(currentElement, stack.size, currentIndex == index)
        }

        if (!visitedLeft.contains(currentIndex)) {
            visitedLeft.add(currentIndex)
            if (left.getP(currentIndex) != NO_INDEX) {
                stack.add(left.getP(currentIndex))
                continue
            }
        }

        if (!nodeProcessed.contains(currentIndex)) {
            visitor.visitNode(currentElement, stack.size, currentIndex == index)
            nodeProcessed.add(currentIndex)
        }

        if (!visitedRight.contains(currentIndex)) {
            visitedRight.add(currentIndex)
            if (right.getP(currentIndex) != NO_INDEX) {
                stack.add(right.getP(currentIndex))
                continue
            }
        }

        visitor.leaveNode(currentElement, stack.size, currentIndex == index)
        stack.removeLast()
    }
}

/**
 * Visitor interface for tree traversal.
 */
interface TreeVisitor<K> {

    fun enterNode(node: K, depth: Int, root: Boolean) {
        // nothing by default
    }

    fun visitNode(node: K, depth: Int, root: Boolean) {
        // nothing by default
    }

    fun leaveNode(node: K, depth: Int, root: Boolean) {
        // nothing by default
    }

}

/**
 * Visitor for determining the height of the whole tree.
 */
private class HeightVisitor<K> : TreeVisitor<K> {
    var height: Int = 0
        private set

    override fun enterNode(node: K, depth: Int, root: Boolean) {
        height = max(height, depth)
    }
}

/**
 * Visitor for the iterative toString implementation.
 */
internal class ToStringVisitor<K> : TreeVisitor<K> {
    private val builder = StringBuilder()
    val result
        get() = builder.toString()

    override fun enterNode(node: K, depth: Int, root: Boolean) {
        builder.append("(")
    }

    override fun visitNode(node: K, depth: Int, root: Boolean) {
        if (root) {
            builder.append(" [$node] ")
        } else {
            builder.append(" $node ")
        }
    }

    override fun leaveNode(node: K, depth: Int, root: Boolean) {
        builder.append(")")
    }
}

/**
 * Iterative height determination using the tree traversal with visitor.
 */
internal fun <K, V> PrimitiveTypeBinaryTree<K, V>.heightI(index: Int): Int {
    val visitor = HeightVisitor<K>()
    traverseTree(index, visitor)
    return visitor.height - 1
}

/**
 * Iterative toString implementation using the tree traversal with visitor.
 */
internal fun <K, V> PrimitiveTypeBinaryTree<K, V>.toStringI(): String {
    val toStringVisitor = ToStringVisitor<K>()
    traverseTree(rootIndex, toStringVisitor)
    return toStringVisitor.result
}

/**
 * Recursive balance check.
 */
fun <K, V> PrimitiveTypeBinaryTree<K, V>.isBalanced() = isBalanced(rootIndex)

internal fun <K, V> PrimitiveTypeBinaryTree<K, V>.isBalanced(index: Int): Boolean {
    if (index == NO_INDEX) {
        return true
    }

    if (maxHeightDifference > 0 && abs(height(left.getP(index)) - height(right.getP(index))) > maxHeightDifference) {
        return false
    }

    return isBalanced(left.getP(index)) && isBalanced(right.getP(index))
}

/**
 * Very inefficient recursive function to balance an arbitrary binary tree. It is an adaptation of a Haskell function that I once implemented :-)
 */
internal fun <K, V> PrimitiveTypeBinaryTree<K, V>.balanceR(index: Int) {
    if (isBalanced(index)) {
        return
    }
    val leftIndex = left[index]
    val rightIndex = right[index]
    if (!isBalanced(leftIndex)) {
        balanceR(leftIndex)
    }
    if (!isBalanced(rightIndex)) {
        balanceR(rightIndex)
    }
    // if both are balanced, the height difference is significant, and we need to adjust for that
    if (height[leftIndex] > height[rightIndex]) {
        // left subtree is higher than the right one --> rotate the whole tree to the right and balance again
        val rightOfLeftIndex = right[leftIndex]
        val leftOfLeftIndex = left[leftIndex]
        if (height[rightOfLeftIndex] > height[leftOfLeftIndex]) {
            // the bigger subtree needs to be kept "outside"
            rotateLeft(leftIndex)
        }
        val newIndex = rotateRight(index)
        balanceR(newIndex)
    } else {
        // right subtree is higher than the left one --> rotate the whole tree to the left and balance again
        val leftOfRightIndex = left[rightIndex]
        val rightOfRightIndex = right[rightIndex]
        if (height[leftOfRightIndex] > height[rightOfRightIndex]) {
            // the bigger subtree needs to be kept "outside"
            rotateRight(rightIndex)
        }
        val newIndex = rotateLeft(index)
        balanceR(newIndex)
    }
}

/**
 * Folding trees has always been a pleasure :-). The given function [f] is applied to the recursive result of the left and the right subtree.
 * The [neutralElement] is the result of an empty subtree.
 */
internal fun <N, K, V> PrimitiveTypeBinaryTree<K, V>.fold(neutralElement: N, index: Int, f: (N, Int, N) -> N): N =
    if (index == NO_INDEX) neutralElement else
        f(fold(neutralElement, left.getP(index), f), index, fold(neutralElement, right.getP(index), f))

/**
 * Height determination using the [fold].
 */
internal fun <K, V> PrimitiveTypeBinaryTree<K, V>.heightR(index: Int) = fold(-1, index) { leftHeight: Int, _, rightHeight: Int ->
    1 + max(leftHeight, rightHeight)
}

/**
 * [toString] implementation using [fold].
 */
fun <K, V> PrimitiveTypeBinaryTree<K, V>.toStringR(): String = fold("", rootIndex) { leftString: String, index: Int, rightString: String ->
    if (index == rootIndex)
        "($leftString [${keys[index]}:${height.getP(index)}] $rightString)"
    else
        "($leftString ${keys[index]}:${height.getP(index)} $rightString)"
}
