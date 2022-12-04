package day01

import common.Solution

typealias ParsedInput = List<List<Int>>

object Day01 : Solution.GroupedLinedInput<ParsedInput>(day = 1) {

    override fun parseInput(input: List<List<String>>): ParsedInput {
        return input.map { it.map(String::toInt) }
    }

    override fun part1(input: ParsedInput): Any {
        return input
            .sortedBy { it.sum() }
            .last()
            .sum()
    }

    override fun part2(input: ParsedInput): Any {
        return input
            .sortedBy { it.sum() }
            .takeLast(3)
            .sumOf { it.sum() }
    }

}

fun main() {
    Day01.solve()
}
