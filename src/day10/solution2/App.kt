package day10.solution2

// Refactored original solution which was pretty messy. Rolled up processing logic into
// a command function.

import common.Solution

typealias ParsedInput = List<String>

fun processCommands(commands: List<String>, fn: (cycle: Int, register: Int) -> Unit) {
    var register = 1
    var cycle = 1

    for (command in commands) {
        when (command) {
            "noop" -> {
                fn(cycle, register)
                cycle += 1
            }
            else -> {
                register += command.split(" ").last().toInt()
            }
        }
    }
}

object Day10 : Solution.LinedInput<ParsedInput>(day = 10) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input
            .map { line ->
                if (line == "noop") listOf(line)
                else listOf("noop", "noop", line)
            }
            .flatten()
    }

    override fun part1(commands: ParsedInput): Any {
        var outcome = listOf<Pair<Int, Int>>()

        processCommands(commands) { cycle, register -> outcome += (cycle to register) }

        var sum = 0
        for (i in 20..220 step 40) {
            sum += outcome[i - 1].let { (c, r) -> c * r }
        }

        return sum
    }

    override fun part2(commands: ParsedInput): Any {
        var screen = mutableListOf<Char>()

        processCommands(commands) { cycle, register ->
            val pixelPos = (cycle - 1) % 40
            val pixel = if (pixelPos in register -1..register + 1) '#' else ' '
            screen.add(pixel)
        }

        screen.chunked(40).forEach { line -> println(line.joinToString("")) }

        return Unit
    }
}

fun main() {
    Day10.solve(test = false)
}