package day03

import common.Puzzle

object Day03 : Puzzle<Int>(day = 3) {
    override fun part1(input: List<String>): Int {
        val sum = input.map { items ->
            val firstCompartment = items.subSequence(0, items.length / 2).toSet()
            val secondCompartment = items.subSequence((items.length / 2), items.length).toSet()
            firstCompartment.intersect(secondCompartment).first()
        }
            .map { item ->
                val priority = when(item) {
                    in 'a'..'z' -> item - 'a' + 1
                    in 'A'..'Z' -> item - 'A' + 27
                    else -> throw error("Invalid item ${item}")
                }
                (item to priority)
            }
            .sumOf { it.second }

        return sum
    }

    override fun part2(input: List<String>): Int {
        val sum = input.chunked(3)
            .map { group -> group.map { it.toSet() }.reduce { acc, item -> acc.intersect(item) }.first() }
            .map { item ->
                val priority = when(item) {
                    in 'a'..'z' -> item - 'a' + 1
                    in 'A'..'Z' -> item - 'A' + 27
                    else -> throw error("Invalid item ${item}")
                }
                (item to priority)
            }
            .sumOf { it.second }

        return sum
    }
}

fun main() {
    Day03.run()
}