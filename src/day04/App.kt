package day04

import common.Solution

typealias Section = Pair<Int, Int>
typealias SectionPair = Pair<Section, Section>
typealias ParsedInput = List<SectionPair>

fun Section.containedIn(section: Section): Boolean {
    return this.first in section.first..section.second && this.second in section.first..section.second
}

fun Section.overlaps(section: Section): Boolean {
    return this.first in section.first..section.second || this.second in section.first..section.second
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
        return input.filter { sectionPair ->
            sectionPair.first.containedIn(sectionPair.second) || sectionPair.second.containedIn(sectionPair.first)
        }.size
    }

    override fun part2(input: ParsedInput): Any {
        return input.filter { sectionPair ->
            sectionPair.first.overlaps(sectionPair.second) || sectionPair.second.overlaps(sectionPair.first)
        }.size
    }
}

fun main() {
    Day04.solve()
}