package day10.solution1

// Original solution used to solve the AoC day 10 puzzle

import common.Solution

typealias ParsedInput = List<String>

object Day10 : Solution.LinedInput<ParsedInput>(day = 10) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input
            .map { line ->
                if (line == "noop") listOf(line)
                else listOf("noop", line, "finish")
            }
            .flatten()
    }

    override fun part1(input: ParsedInput): Any {
        var prepvalue = 0
        var register = 1
        var cycle = 1
        var outcome = listOf<Triple<Int, Int, String>>()

        for (line in input) {
            when (line) {
                "noop" -> {
                    outcome += Triple(cycle, register, line)
                    cycle += 1
                }
                "finish" -> {
                    register += prepvalue
                    prepvalue = 0
                }
                else -> {
                    val value = line.split(" ").last().toInt()
                    prepvalue = value
                    outcome += Triple(cycle, register, line)
                    cycle += 1
                }
            }
        }

        val interestedCycles = setOf(20, 60, 100, 140, 180, 220)

        return outcome
            .filter { (c) -> c % 20 == 0 }.map { (c, r) -> (c to r) }
            .filter { (c) -> c in interestedCycles }
            .sumOf { (c, r) -> c * r}
    }

    override fun part2(input: ParsedInput): Any {
        var screen = mutableListOf<Char>()
        var cycle = 1
        var register = 1
        var prepvalue = 0

        fun drawPixel() {
            val pixelPos = (cycle - 1) % 40
            val pixel = if (pixelPos in register -1..register + 1) '#' else '.'
            screen.add(pixel)
        }

        for (line in input) {
            when (line) {
                "noop" -> {
                    drawPixel()
                    cycle += 1
                }
                "finish" -> {
                    register += prepvalue
                    prepvalue = 0
                }
                else -> {
                    drawPixel()
                    val value = line.split(" ").last().toInt()
                    prepvalue = value
                    cycle += 1
                }
            }
        }

        screen.chunked(40).forEach { line -> println(line.joinToString("")) }

        return Unit
    }
}

fun main() {
    Day10.solve(test = false)
}