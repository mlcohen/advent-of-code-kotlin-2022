package day04

import common.Solution

typealias Section = Pair<Int, Int>
typealias SectionPair = Pair<Section, Section>
typealias ParsedInput = List<SectionPair>

fun Section.containedIn(s: Section): Boolean {
    return this.first in s.first..s.second && this.second in s.first..s.second
}

fun Section.overlaps(s: Section): Boolean {
    return this.first in s.first..s.second || this.second in s.first..s.second
}

object Day04 : Solution.LinedInput<ParsedInput>(day = 4) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input.map { line ->
            line.split(',').map { range ->
                range.split('-').map(String::toInt).let { (a, b) -> (a to b) }
            }.let { (r1, r2) -> (r1 to r2) }
        }
    }

    override fun part1(input: ParsedInput): Any {
        return input.count { sectionPair ->
            sectionPair.first.containedIn(sectionPair.second) || sectionPair.second.containedIn(sectionPair.first)
        }
    }

    override fun part2(input: ParsedInput): Any {
        return input.count { sectionPair ->
            sectionPair.first.overlaps(sectionPair.second) || sectionPair.second.overlaps(sectionPair.first)
        }
    }
}

fun main() {
    Day04.solve()
}