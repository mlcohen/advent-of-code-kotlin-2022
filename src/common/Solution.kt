package common

sealed class Solution<T, I> {

    private val day: Int;

    constructor(day: Int) {
        this.day = day
    }

    fun solve(test: Boolean = false) {
        val filename = "input/day${day.toString().padStart(2, padChar = '0')}${if (test) "_test" else ""}"
        val preprocessedInput = preprocessInput(filename)
        var parsedInput = parseInput(preprocessedInput)
        println("Solution for day ${day}\n")
        println("*** part 1")
        println("result = ${part1(parsedInput)}")
        println()
        println("*** part 2")
        println("result = ${part2(parsedInput)}")
    }

    protected abstract fun preprocessInput(filename: String): T;

    abstract fun parseInput(input: T): I;

    abstract fun part1(input: I): Any;

    abstract fun part2(input: I): Any;

    abstract class LinedInput<I> : Solution<List<String>, I> {

        constructor(day: Int): super(day)

        override fun preprocessInput(filename: String): List<String>
                = readInputLines(filename)

        abstract override fun part1(input: I): Any;

        abstract override fun part2(input: I): Any;

    }

    abstract class GroupedLinedInput<I> : Solution<List<List<String>>, I> {

        constructor(day: Int): super(day)

        override fun preprocessInput(filename: String): List<List<String>>
                = readInputGroupedLines(filename)

        abstract override fun part1(input: I): Any;

        abstract override fun part2(input: I): Any;

    }

}