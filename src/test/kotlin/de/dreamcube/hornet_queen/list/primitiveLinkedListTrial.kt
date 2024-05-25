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