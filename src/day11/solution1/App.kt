package day11.solution1

// Original solution used to solve the AoC day 11 puzzle.

import common.Solution
import kotlin.math.floor

data class MonkeyTest(val divBy: Int, val toMonkeyIfTrue: Int, val toMonkeyIfFalse: Int) {
    fun test(worryLevel: Int): Int {
        val testResult = worryLevel % divBy == 0
        return if (testResult) toMonkeyIfTrue else toMonkeyIfFalse
    }
}
data class MonkeyTossResult(val item: Int, val tossToMonkeyId: Int, val state: Monkey)

data class Monkey(val items: List<Int>, val operation: String, val test: MonkeyTest) {

    fun hasItems(): Boolean = !items.isEmpty()
    fun tossNextItem(): MonkeyTossResult {
        val tossedItem = items.first()
        val (_, op, arg2) = operation.split(" ")
        val worryLevel = when (op) {
            "*" -> if (arg2 == "old") tossedItem * tossedItem else tossedItem * arg2.toInt()
            "+" -> tossedItem + arg2.toInt()
            else -> throw error("Invalid operator $op")
        }

        val newWorryLevel = floor(worryLevel.toDouble() / 3.0).toInt()
        val tossToMonkeyId = test.test(newWorryLevel)
        val remainingItems = items.drop(1)

        return MonkeyTossResult(newWorryLevel, tossToMonkeyId, this.copy(items = remainingItems))
    }
    fun catchItem(item: Int): Monkey {
        return this.copy(items = items + item)
    }
}

typealias Inspections = Map<Int, Int>
data class RoundResult(val monkeys: List<Monkey>, val inspections: Inspections)

fun round(monkeys: List<Monkey>): RoundResult {
    val roundState = mutableMapOf<Int, Monkey>()
    var inspections = mutableMapOf<Int, Int>()

    monkeys.forEachIndexed { id, monkey ->
        roundState[id] = monkey
        inspections[id] = 0
    }

    for (currentMonkeyId in monkeys.indices) {
        var currentMonkey = roundState[currentMonkeyId]!!
        while (currentMonkey.hasItems()) {
            val (item, tossToMonkeyId, currentMonkeyNextState) = currentMonkey.tossNextItem()
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

typealias ParsedInput = List<Monkey>

object Day11 : Solution.GroupedLinedInput<ParsedInput>(day = 11) {

    override fun parseInput(input: List<List<String>>): ParsedInput {
        return input.map { block ->
            val processed = block.drop(1).map { it.split(":").last().trim() }
            val items = processed[0].split(",").map { it.trim().toInt() }
            val operation = processed[1].split("=").last().trim()
            val testDivBy = processed[2].split(" ").last().toInt()
            val ifTrue = processed[3].split(" ").last().toInt()
            val ifFalse = processed[4].split(" ").last().toInt()
            Monkey(items, operation, MonkeyTest(testDivBy, ifTrue, ifFalse))
        }
    }

    override fun part1(input: ParsedInput): Any {
        val outcomes = (1..20).toList().fold(listOf((input to mapOf<Int, Int>()))) { list, _ ->
            val (monkeys, _) = list.last()
            val result = round(monkeys)
            list + (result.monkeys to result.inspections)
        }

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

        return totalInspections.sorted().takeLast(2).let { (a, b) -> a * b }
    }

    override fun part2(input: ParsedInput): Any {
        return Unit
    }
}

fun main() {
    Day11.solve(test = false)
}