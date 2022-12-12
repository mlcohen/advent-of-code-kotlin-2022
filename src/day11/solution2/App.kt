package day11.solution2

// Refactored original Day 11 solution.

import common.Solution
import kotlin.math.floor

interface MonkeyInspectionDelegate {
    fun monkeyDidFinishInspectingItem(worryLevel: Long): Long;
}

data class MonkeyInspectNextItemResult(val item: Long, val tossToMonkeyId: Int, val state: Monkey)

data class Monkey(
    val worryLevelItems: List<Long>,
    val operation: String,
    val testDivisor: Int,
    val toMonkeyWhenTrue: Int,
    val toMonkeyWhenFalse: Int,
    ) {
    fun hasItems(): Boolean = worryLevelItems.isNotEmpty()

    fun inspectNextItem(delegate: MonkeyInspectionDelegate): MonkeyInspectNextItemResult {
        val oldWorryLevel = worryLevelItems.first()
        val (_, op, arg2) = operation.split(" ")
        val newWorryLevel = when (op) {
            "*" -> if (arg2 == "old") oldWorryLevel * oldWorryLevel else oldWorryLevel * arg2.toLong()
            "+" -> oldWorryLevel + arg2.toLong()
            else -> throw error("Invalid operator $op")
        }
        val moderatedNewWorryLevel = delegate.monkeyDidFinishInspectingItem(newWorryLevel)
        val tossToMonkeyId = if (moderatedNewWorryLevel % testDivisor == 0L) {
            toMonkeyWhenTrue
        } else {
            toMonkeyWhenFalse
        }
        val remainingItems = worryLevelItems.drop(1)

        return MonkeyInspectNextItemResult(
            moderatedNewWorryLevel,
            tossToMonkeyId,
            this.copy(worryLevelItems = remainingItems),
        )
    }

    fun catchItem(item: Long): Monkey {
        return this.copy(worryLevelItems = worryLevelItems + item)
    }
}

data class RoundResult(val monkeys: List<Monkey>, val inspections: RoundMonkeyInspectionList)

typealias RoundMonkeyInspectionList = List<Int>
typealias SimulatedRoundsResult = List<RoundResult>

fun SimulatedRoundsResult.talliedInspections(): List<Int> {
    val monkeyCount = this.first().monkeys.size
    return this.map { it.inspections }.let { inspectionsList ->
        val tally = MutableList(monkeyCount) { 0 }
        inspectionsList.forEach { inspectionRound ->
            inspectionRound.forEachIndexed { idx, value ->
                tally[idx] += value
            }
        }
        tally.toList()
    }
}

fun SimulatedRoundsResult.levelOfMonkeyBusiness(): Long {
    return this.talliedInspections()
        .sorted()
        .takeLast(2)
        .let { (a, b) -> a.toLong() * b.toLong() }
}

fun SimulatedRoundsResult.printRoundOutcomeDetails() {
    this.forEachIndexed { i, round ->
        println("After round ${i + 1}")
        round.monkeys.forEachIndexed { id, monkey ->
            println("Monkey $id: ${monkey.worryLevelItems}")
        }
        println()
    }
}

fun SimulatedRoundsResult.printTalliedInspectionDetails() {
    this.talliedInspections().forEachIndexed { idx, i ->
        println("Monkey $idx inspected items $i times")
    }
}

class MonkeyKeepAwaySimulation(
    private val monkeyInspectionDelegate: MonkeyInspectionDelegate,
) {
    fun runRound(
        monkeys: List<Monkey>,
    ): RoundResult {
        val roundState = monkeys.toMutableList()
        val inspections = MutableList(monkeys.size) { 0 }

        for (currentMonkeyId in monkeys.indices) {
            var currentMonkey = roundState[currentMonkeyId]!!
            while (currentMonkey.hasItems()) {
                val (item, tossToMonkeyId, currentMonkeyNextState) =
                    currentMonkey.inspectNextItem(this.monkeyInspectionDelegate)

                val tossedToMonkey = roundState[tossToMonkeyId]!!
                val tossedToMonkeyNextState = tossedToMonkey.catchItem(item)

                roundState[currentMonkeyId] = currentMonkeyNextState
                roundState[tossToMonkeyId] = tossedToMonkeyNextState
                inspections[currentMonkeyId] = inspections[currentMonkeyId]!! + 1

                currentMonkey = currentMonkeyNextState
            }
        }

        return RoundResult(
            monkeys = roundState.toList(),
            inspections = inspections.toList(),
        )
    }

    fun simulate(
        initMonkeysState: List<Monkey>,
        roundCount: Int,
    ): SimulatedRoundsResult {
        val roundResults = mutableListOf(RoundResult(initMonkeysState, listOf()))
        repeat(roundCount) {
            val (monkeys, _) = roundResults.last()
            val result = this.runRound(monkeys)
            roundResults.add(result)
        }
        return roundResults.toList().drop(1)
    }
}

typealias ParsedInput = List<Monkey>

object Day11 : Solution.GroupedLinedInput<ParsedInput>(day = 11) {

    override fun parseInput(input: List<List<String>>): ParsedInput {
        return input.map { it.drop(1)
                .map { lines -> lines.split(":").last().trim() }
                .let { (a, b, c, d, e) ->
                    val items = a.split(",").map { item -> item.trim().toLong() }
                    val operation = b.split("=").last().trim()
                    val testDivisor = c.split(" ").last().toInt()
                    val toMonkeyWhenTrue = d.split(" ").last().toInt()
                    val toMonkeyWhenFalse = e.split(" ").last().toInt()
                    Monkey(items, operation, testDivisor, toMonkeyWhenTrue, toMonkeyWhenFalse)
                }
        }
    }

    override fun part1(monkeys: List<Monkey>): Any {
        val monkeyInspectionDelegate = object: MonkeyInspectionDelegate {
            override fun monkeyDidFinishInspectingItem(worryLevel: Long): Long {
                return floor(worryLevel / 3.0).toLong()
            }
        }

        val sim = MonkeyKeepAwaySimulation(monkeyInspectionDelegate)
        val result = sim.simulate(monkeys, 20)

//        result.printRoundOutcomeDetails()
//        result.printTalliedInspectionDetails()

        return result.levelOfMonkeyBusiness()
    }

    override fun part2(monkeys: List<Monkey>): Any {
        val testDivisorProduct = monkeys.map { it.testDivisor }.reduce { a, b -> a * b}
        val monkeyInspectionDelegate = object: MonkeyInspectionDelegate {
            override fun monkeyDidFinishInspectingItem(worryLevel: Long): Long {
                return worryLevel % testDivisorProduct
            }
        }

        val sim = MonkeyKeepAwaySimulation(monkeyInspectionDelegate)
        val result = sim.simulate(monkeys, 10_000)

//        result.printRoundOutcomeDetails()
//        result.printTalliedInspectionDetails()

        return result.levelOfMonkeyBusiness()
    }
}

fun main() {
    Day11.solve(test = false)
}