package com.ralf.bottomsheet.demo

data class Item(val text: String)

fun getDemoList(): List<Item> {
    return mutableListOf<Item>().apply {
        (0..100).forEach { i ->
            add(Item("title - $i"))
        }
    }
}

fun getDemoList2(): List<Item> {
    return mutableListOf<Item>().apply {
        (101..200).forEach { i ->
            add(Item("title - $i"))
        }
    }
}

