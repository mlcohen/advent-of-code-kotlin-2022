package common

sealed class Solution<T> {

    private val day: Int;

    constructor(day: Int) {
        this.day = day
    }

    fun solve(test: Boolean = false) {
        val filename = "input/day${day.toString().padStart(2, padChar = '0')}${if (test) "_test" else ""}"
        val input = processInput(filename)
        println("Solution for day ${day}\n")
        println("*** part 1")
        println("result = ${part1(input)}")
        println()
        println("*** part 2")
        println("result = ${part2(input)}")
    }

    protected abstract fun processInput(filename: String): T;

    abstract fun part1(input: T): Any;

    abstract fun part2(input: T): Any;

    abstract class LinedInput : Solution<List<String>> {

        constructor(day: Int): super(day)

        override fun processInput(filename: String): List<String>
                = readInputLines(filename)

        abstract override fun part1(input: List<String>): Any;

        abstract override fun part2(input: List<String>): Any;

    }

    abstract class GroupedLinedInput : Solution<List<List<String>>> {

        constructor(day: Int): super(day)

        override fun processInput(filename: String): List<List<String>>
                = readInputGroupedLines(filename)

        abstract override fun part1(input: List<List<String>>): Any;

        abstract override fun part2(input: List<List<String>>): Any;

    }

}