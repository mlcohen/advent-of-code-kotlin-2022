package day03

import common.Solution

object Rucksack {
    fun priorityForItem(item: Char): Int = when(item) {
        in 'a'..'z' -> item - 'a' + 1
        in 'A'..'Z' -> item - 'A' + 27
        else -> throw error("Invalid item ${item}")
    }
}

object Day03 : Solution.LinedInput(day = 3) {
    override fun part1(input: List<String>): Any {
        val sum = input.map { items ->
            val firstCompartment = items.subSequence(0, items.length / 2).toSet()
            val secondCompartment = items.subSequence((items.length / 2), items.length).toSet()
            firstCompartment.intersect(secondCompartment).first()
        }
            .map { item ->
                val priority = Rucksack.priorityForItem(item)
                (item to priority)
            }
            .sumOf { it.second }

        return sum
    }

    override fun part2(input: List<String>): Any {
        val sum = input.chunked(3)
            .map { group -> group.map { it.toSet() }.reduce { acc, item -> acc.intersect(item) }.first() }
            .map { item ->
                val priority = Rucksack.priorityForItem(item)
                (item to priority)
            }
            .sumOf { it.second }

        return sum
    }
}

fun main() {
    Day03.solve()
}