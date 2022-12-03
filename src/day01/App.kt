package day01

import common.Solution

object Day01 : Solution.GroupedLinedInput(day = 1) {

    override fun part1(input: List<List<String>>): Any {
        return input
            .map { it.map(String::toInt) }
            .sortedBy { it.sum() }
            .last()
            .sum()
    }

    override fun part2(input: List<List<String>>): Any {
        return input
            .map { it.map(String::toInt) }
            .sortedBy { it.sum() }
            .takeLast(3)
            .sumOf { it.sum() }
    }

}

fun main() {
    Day01.solve()
}
