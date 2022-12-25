package day16

// Original solution used to solve the AoC day 16 puzzle

import common.Solution

data class Valve(val label: String, val flowRate: Int, val tunnels: List<String>)

typealias ValveList = List<Valve>
typealias ValveGraph = Map<String, Valve>

enum class Action { OPENED_VALVE, MOVED_TO_VALVE }

data class StepTaken(val action: Action, val valve: Valve, val time: Int)

typealias StepsTaken = List<StepTaken>

fun StepsTaken.pressureRelease(maxTime: Int): Int {
    return this
        .filter { it.action == Action.OPENED_VALVE }
        .sumOf { (maxTime - it.time) * it.valve.flowRate }
}

fun StepsTaken.openValves(): List<String> {
    return this
        .filter { it.action == Action.OPENED_VALVE }
        .map { it.valve.label }
        .sorted()
}

typealias TraverseCacheKey = Triple<String, Int, List<String>>
typealias TraverseCacheMap = MutableMap<TraverseCacheKey, StepsTaken>

class TraverseValves(
    private val valves: ValveList,
    private val maxTime: Int,
) {
    private val graph: ValveGraph = valves.associateBy { it.label }
    private val workingValves: Set<String> = valves.filter { it.flowRate > 0 }.map { it.label }.toSet()
    private val cache: TraverseCacheMap = mutableMapOf()

    fun traverse(): StepsTaken {
        val openedValves = setOf<String>()
        val traversalValveCounters = valves.associate { it.label to it.tunnels.size }
        val result = takeNextStep(graph["AA"]!!,
            openedValves = openedValves,
            traversalValveCounters = traversalValveCounters,
        )
        return result
    }

    fun takeNextStep(
        valve: Valve,
        time: Int = 0,
        openedValves: Set<String>,
        traversalValveCounters: Map<String, Int>,
    ): StepsTaken {
//        println("== Minute $time: At valve ${valve.label} ==")

        if (openedValves.size == workingValves.size || time == maxTime) {
            return listOf()
        }

        val timeRemaining = maxTime - time
        val cacheKey = Triple(valve.label, timeRemaining, openedValves.sorted())

        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]!!
        }

        val results = mutableListOf<StepsTaken>()
        if (valve.flowRate > 0) {
            val openValveResult = openValve(
                valve,
                time, openedValves, traversalValveCounters
            )
            results.add(openValveResult)
        }
        val moveToValveResult = moveToConnectedValves(valve,
            time, openedValves, traversalValveCounters)
        results.add(moveToValveResult)

        val bestResult = results.maxBy { it.pressureRelease(maxTime) }

        cache[cacheKey] = bestResult

        return bestResult
    }

    fun openValve(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
        traversalValveCounters: Map<String, Int>,
    ): StepsTaken {
        if (valve.label in openedValves) {
            return listOf()
        }
//        println("You open valve ${valve.label}")
        val nextTime = time + 1
        val nextOpenedValves = openedValves + valve.label
        val stepTaken = StepTaken(Action.OPENED_VALVE, valve, nextTime)

        return listOf(stepTaken) + takeNextStep(valve,
            nextTime,
            nextOpenedValves,
            traversalValveCounters,
        )
    }

    fun moveToConnectedValves(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
        traversalValveCounters: Map<String, Int>,
    ): StepsTaken {
        val outcomes = mutableListOf<StepsTaken>()

        for (nextValveLabel in valve.tunnels) {
            val nextValve = graph[nextValveLabel]!!
            val outcome = moveToValve(nextValve,
                time = time,
                openedValves = openedValves,
                traversalValveCounters = traversalValveCounters,
            )

            outcomes.add(outcome)
        }

        if (outcomes.isEmpty()) {
            return listOf()
        }

        return outcomes.maxBy { it.pressureRelease(maxTime) }
    }

    fun moveToValve(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
        traversalValveCounters: Map<String, Int>,
    ): StepsTaken {
        val nextValveTraveralCounter = traversalValveCounters[valve.label]!!

        if (nextValveTraveralCounter == 0) {
            return listOf()
        }
//        println("You move to valve ${valve.label}. counter = $nextValveTraveralCounter")

        val nextTime = time + 1
        val stepTaken = StepTaken(Action.MOVED_TO_VALVE, valve, nextTime)
        val nextTraversalValveCounts = traversalValveCounters + (valve.label to nextValveTraveralCounter - 1)

        return listOf(stepTaken) + takeNextStep(valve,
            time = nextTime,
            openedValves = openedValves,
            traversalValveCounters = nextTraversalValveCounts,
        )
    }

}

object Day16 : Solution.LinedInput<ValveList>(day = 16) {

    override fun parseInput(input: List<String>): ValveList {
        return input.map{ line ->
            line.split(';').let { (a, b) ->
                val left = a.split(' ', '=')
                    .let { tokens -> (tokens[1] to tokens.last().toInt()) }
                val right = b.trim().split(' ', ',')
                    .let { tokens -> tokens.drop(4).filter(String::isNotEmpty) }
                Valve(left.first, left.second, right)
            }
        }
    }

    override fun part1(input: ValveList): Any {
        val traverser = TraverseValves(input, 30)
        val result = traverser.traverse()

        result.forEach {
            println("Minute ${it.time}: valve = ${it.valve.label}, action = ${it.action}")
        }

        return result.pressureRelease(30)
    }

    override fun part2(input: ValveList): Any {
        return Unit
    }
}

fun main() {
    Day16.solve(test = false)
}