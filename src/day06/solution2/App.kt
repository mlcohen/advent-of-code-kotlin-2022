package day06.solution2

// Took my original solution and cleaned it up. Got rid of redundant code.
// Also realized that I just ended up recreating the windowing function which is
// already part of kotlin. Sigh. <facepalm> :-/

import common.Solution

typealias ParsedInput = List<String>

fun scanForMarkerEnd(buffer: String, distinctChars: Int = 4): Int {
    var markerStartPos = 0
    var seq = ""
    var i = 0

    while ((markerStartPos + i) < buffer.length - 1) {
        val c = buffer[markerStartPos + i]

        if (c in seq.toSet()) {
            markerStartPos += 1
            seq = "" + buffer[markerStartPos]
            i = 1
            continue
        }

        seq += c
        i += 1

        if (seq.length == distinctChars) {
            return markerStartPos + seq.length
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