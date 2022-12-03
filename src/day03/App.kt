package day03

import java.io.File

fun runSolutionPart1(input: List<String>) {
    println("*** Solution for Day 3, part 1")

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

    println("sum = $sum")
}

fun runSolutionPart2(input: List<String>) {
    println("*** Solution for Day 3, part 2")

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

    println("sum = $sum")
}

fun main() {
    val input = File("input/day03").readLines()
    runSolutionPart1(input)
    runSolutionPart2(input)
}