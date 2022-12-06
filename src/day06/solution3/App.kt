package day06.solution3

// Solution using the actual kotlin windowed function. Waaaaay simpler.

import common.Solution

typealias ParsedInput = List<String>

fun scanForMarkerEnd(str: String, distinctChars: Int = 4): Int {
    return str.toList()
        .windowed(distinctChars)
        .dropWhile { it.toSet().size != distinctChars }
        .first()
        .let {
            str.indexOf(it.joinToString("")) + distinctChars
        }
}

object Day06 : Solution.LinedInput<ParsedInput>(day = 6) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input
    }

    override fun part1(input: ParsedInput): Any {
        return scanForMarkerEnd(input.first(), distinctChars = 4)
    }

    override fun part2(input: ParsedInput): Any {
        return scanForMarkerEnd(input.first(), distinctChars = 14)
    }
}

fun main() {
    Day06.solve(test = false)
}