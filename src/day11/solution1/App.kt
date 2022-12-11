package day11.solution1

// Original solution used to solve the AoC day 11 puzzle.

// Ugh. Really messed up trying to get my head wrapped round making sense of part 2.
// Took me hours to figure it out. Code is also a mess. :_(

import common.Solution
import kotlin.math.floor

data class MonkeyTest(val divBy: Int, val toMonkeyIfTrue: Int, val toMonkeyIfFalse: Int) {
    fun test(worryLevel: Long): Int {
        val testResult = worryLevel % divBy.toLong() == 0L
        return if (testResult) toMonkeyIfTrue else toMonkeyIfFalse
    }
}
data class MonkeyTossResult(val item: Long, val tossToMonkeyId: Int, val state: Monkey)

data class Monkey(val items: List<Long>, val operation: String, val test: MonkeyTest) {

    fun hasItems(): Boolean = items.isNotEmpty()
    fun tossNextItem(reliefMod: (Long) -> Long): MonkeyTossResult {
        val tossedItem = items.first()
        val (_, op, arg2) = operation.split(" ")
        val worryLevel = when (op) {
            "*" -> if (arg2 == "old") tossedItem * tossedItem else tossedItem * arg2.toLong()
            "+" -> tossedItem + arg2.toLong()
            else -> throw error("Invalid operator $op")
        }

        val modWorryLevel = reliefMod(worryLevel)
        val tossToMonkeyId = test.test(modWorryLevel)
        val remainingItems = items.drop(1)

        return MonkeyTossResult(modWorryLevel, tossToMonkeyId, this.copy(items = remainingItems))
    }
    fun catchItem(item: Long): Monkey {
        return this.copy(items = items + item)
    }
}

typealias Inspections = Map<Int, Int>
data class RoundResult(val monkeys: List<Monkey>, val inspections: Inspections)

fun round(monkeys: List<Monkey>, reliefMod: (Long) -> Long): RoundResult {
    val roundState = mutableMapOf<Int, Monkey>()
    var inspections = mutableMapOf<Int, Int>()

    monkeys.forEachIndexed { id, monkey ->
        roundState[id] = monkey
        inspections[id] = 0
    }

    for (currentMonkeyId in monkeys.indices) {
        var currentMonkey = roundState[currentMonkeyId]!!
        while (currentMonkey.hasItems()) {
            val (item, tossToMonkeyId, currentMonkeyNextState) = currentMonkey.tossNextItem(reliefMod)
            val tossedToMonkey = roundState[tossToMonkeyId]!!
            val tossedToMonkeyNextState = tossedToMonkey.catchItem(item)
            roundState[currentMonkeyId] = currentMonkeyNextState
            roundState[tossToMonkeyId] = tossedToMonkeyNextState
            inspections[currentMonkeyId] = inspections[currentMonkeyId]!! + 1
            currentMonkey = currentMonkeyNextState
        }
    }

    return RoundResult(
        monkeys = List(monkeys.size) { idx -> roundState[idx]!! },
        inspections = inspections.toMap(),
    )
}

typealias SimulateRoundsResult = List<Pair<List<Monkey>, Map<Int, Int>>>

fun simulateRounds(initMonkeyState: List<Monkey>, numRounds: Int, reliefMod: (Long) -> Long): SimulateRoundsResult {
    return (1..numRounds).toList().fold(listOf((initMonkeyState to mapOf<Int, Int>()))) { list, _ ->
        val (monkeys, _) = list.last()
        val result = round(monkeys, reliefMod)
        list + (result.monkeys to result.inspections)
    }
}

typealias ParsedInput = List<Monkey>

object Day11 : Solution.GroupedLinedInput<ParsedInput>(day = 11) {

    override fun parseInput(input: List<List<String>>): ParsedInput {
        return input.map { block ->
            block.drop(1).map { it.split(":").last().trim() }.let { config ->
                val items = config[0].split(",").map { it.trim().toLong() }
                val operation = config[1].split("=").last().trim()
                val divBy = config[2].split(" ").last().toInt()
                val ifTrue = config[3].split(" ").last().toInt()
                val ifFalse = config[4].split(" ").last().toInt()
                Monkey(items, operation, MonkeyTest(divBy, ifTrue, ifFalse))
            }
        }
    }

    override fun part1(input: ParsedInput): Any {
        val outcomes = simulateRounds(input, 20) { worryLevel -> floor(worryLevel / 3.0).toLong() }

        outcomes.forEachIndexed { i, result ->
            println("After round ${i + 1}")
            result.first.forEachIndexed { id, monkey ->
                println("Monkey $id: ${monkey.items}")
            }
            println()
        }

        val totalInspections = MutableList(input.size) { 0 }

        for (outcome in outcomes) {
            val (_, inspections) = outcome
            inspections.forEach { (id, count) ->
                totalInspections[id] += count
            }
        }

        totalInspections.forEachIndexed { idx, i ->
            println("Monkey $idx inspected items $i times")
        }

        return totalInspections.sorted().takeLast(2).let { (a, b) -> a.toLong() * b.toLong() }
    }

    override fun part2(input: ParsedInput): Any {
        val relief = input.map { it.test.divBy }.reduce { a, b -> a * b}
        val outcomes = simulateRounds(input, 10_000) { worryLevel -> worryLevel % relief }

        listOf(listOf(1, 20), (1000..10000 step 1000).toList()).flatten().forEach { rounds ->
            val totalInspections = MutableList(input.size) { 0 }

            for (outcome in outcomes.drop(1).take(rounds)) {
                val (_, inspections) = outcome
                inspections.forEach { (id, count) ->
                    totalInspections[id] += count
                }
            }

            println("== After round $rounds ==")
            totalInspections.forEachIndexed { idx, i ->
                println("Monkey $idx inspected items $i times")
            }
            println()
        }

        val totalInspections = MutableList(input.size) { 0 }

        for (outcome in outcomes) {
            val (_, inspections) = outcome
            inspections.forEach { (id, count) ->
                totalInspections[id] += count
            }
        }

        return totalInspections.sorted().takeLast(2).let { (a, b) -> a.toLong() * b.toLong() }
    }
}

fun main() {
    Day11.solve(test = false)
}