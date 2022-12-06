package day06.solution1

// Original solution used to solve the AoC day 6 puzzle. Warts and all.

import common.Solution

typealias ParsedInput = List<String>

fun scanForMarkerEnd(buffer: String, distinctChars: Int = 4): Int {
    var markerStartPos = 0
    var s = mutableSetOf<Char>()
    var seq = mutableListOf<Char>()
    var i = 0

    while ((markerStartPos + i) < buffer.length - 1) {
        val c = buffer[markerStartPos + i]

        if (c in s) {
            s.clear()
            seq.clear()
            markerStartPos += 1
            val nextChar = buffer[markerStartPos]
            seq.add(nextChar)
            s.add(nextChar)
            i = 1
            continue
        }

        seq.add(c)
        s.add(c)
        i += 1

        if (seq.size >= distinctChars) {
            return markerStartPos + seq.size
        }
    }

    return -1
}

object Day06 : Solution.LinedInput<ParsedInput>(day = 6) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input
    }

    override fun part1(input: ParsedInput): Any {
        return scanForMarkerEnd(input.first())
    }

    override fun part2(input: ParsedInput): Any {
        return scanForMarkerEnd(input.first(), distinctChars = 14)
    }
}

fun main() {
    Day06.solve(test = false)
}