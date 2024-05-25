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

}