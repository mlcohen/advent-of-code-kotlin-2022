package common

abstract class Puzzle<T> {

    private val day: Int;

    constructor(day: Int) {
        this.day = day
    }

    fun run(test: Boolean = false) {
        val filename = "input/day${day.toString().padStart(2, padChar = '0')}${if (test) "_test" else ""}"
        val input = readInputLines(filename)
        println("*** Solution for day ${day}, part 1")
        val resultPart1 = part1(input)
        println("result = $resultPart1")
        println()
        println("*** Solution for day ${day}, part 1")
        val resultPart2 = part2(input)
        println("result = $resultPart2")
    }

    abstract fun part1(input: List<String>): T;

    abstract fun part2(input: List<String>): T;

}