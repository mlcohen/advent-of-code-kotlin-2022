package org.aoc

import java.io.File

fun processInput(lines: List<String>): List<List<Int>> {
    return lines.fold(mutableListOf(mutableListOf<Int>())) { groups, line ->
        if (line.isEmpty()) {
            groups.add(mutableListOf())
        } else {
            groups.last().add(line.toInt())
        }
        groups
    }
}

fun runSolutionPart1(input: List<List<Int>>) {
    val answer = input.sortedBy { it.sum() }.last().sum()
    println("*** Solution for Day 1, part 1")
    println("answer: $answer")
}

fun runSolutionPart2(input: List<List<Int>>) {
    val answer = input.sortedBy { it.sum() }.takeLast(3).sumOf { it.sum() }
    println("*** Solution for Day 1, part 2")
    println("answer: $answer")
}

fun main() {
    val rawInput = File("day01/src/main/resources/input.txt").readLines()
    val input = processInput(rawInput)

    runSolutionPart1(input)
    runSolutionPart2(input)
}
