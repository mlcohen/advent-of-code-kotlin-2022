package day16

// Original solution used to solve the AoC day 16 puzzle

import common.Solution
import kotlin.concurrent.timerTask
import kotlin.math.max
import kotlin.math.pow

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

typealias TraverseCacheKey = Triple<String, Int, List<String>>
typealias TraverseCacheMap = MutableMap<TraverseCacheKey, StepsTaken>

class TraverseValves(
    private val valves: ValveList,
    private val maxTime: Int,
    private val skipValves: Set<String> = setOf(),
) {
    private val graph: ValveGraph = valves.associateBy { it.label }
    private val workingValves: Set<String> = valves
        .filter { it.flowRate > 0 }
        .map { it.label }
        .toSet()
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

        val openValveResult = openValve(
            valve,
            time, openedValves, traversalValveCounters
        )

        val moveToValveResult = moveToConnectedValves(valve,
            time, openedValves, traversalValveCounters)

        val bestResult = listOf(openValveResult, moveToValveResult).maxBy { it.pressureRelease(maxTime) }

        cache[cacheKey] = bestResult

        return bestResult
    }

    fun openValve(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
        traversalValveCounters: Map<String, Int>,
    ): StepsTaken {
        if (valve.label in openedValves || valve.flowRate == 0 || valve.label in skipValves) {
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
            if (nextValveLabel in skipValves) {
                continue
            }

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
//        val traverser = TraverseValves(input, 30)
//        val result = traverser.traverse()
//
//        result.forEach {
//            println("Minute ${it.time}: valve = ${it.valve.label}, action = ${it.action}")
//        }
//
//        return result.pressureRelease(30)
        return Unit
    }

    override fun part2(input: ValveList): Any {
        fun shortestPaths(source: String, graph: ValveGraph): Pair<Map<String, Int>, Map<String, String>> {
            val dist = graph.keys.associate { it to Int.MAX_VALUE }.toMutableMap()
            val prev = mutableMapOf<String, String>()
            val queue = graph.keys.toMutableSet()
            dist[source] = 0

            while (queue.isNotEmpty()) {
                val u = queue.minBy { dist[it]!! }
                queue.remove(u)

                for (v in graph[u]!!.tunnels) {
                    if (v !in queue) {
                        continue
                    }
                    val alt = dist[u]!! + 1
                    if (alt < dist[v]!!) {
                        dist[v] = alt
                        prev[v] = u
                    }
                }
            }

            return dist.toMap() to prev.toMap()
        }

//        val graph = input.associateBy { it.label }
//        val result = shortestPaths("AA", graph)

        val allWorkingValves = input.filter { it.flowRate > 0 }.map { it.label }

        fun filterValves(v: Int): Set<String> {
            val l = mutableListOf<String>()
            for (i in 0 until allWorkingValves.size) {
                val o = 0x0001 shl i and v
                if (o > 0) {
                    l.add(allWorkingValves[i])
                }
            }
            return l.toSet()
        }

        val outcomes = mutableListOf<Triple<Int, Set<String>, Set<String>>>()
        val maxPermuations = (2.0.pow(allWorkingValves.size).toInt() / 2)
        for (i in 1 until maxPermuations) {
            val skipValves = filterValves(i)
            val youSkipValves = skipValves
            val elephantSkipValves = allWorkingValves.toSet() - skipValves
            if (i % 100 == 0) {
                print("Attempt $i: ")
            }
//            println("... you skip $youSkipValves")
//            println("... elephant skip $elephantSkipValves")
            val youTraverser = TraverseValves(input, 26, youSkipValves)
            val elephantTraverser = TraverseValves(input, 26, elephantSkipValves)
            val youStepsTaken = youTraverser.traverse()
            val elephantStepsTaken = elephantTraverser.traverse()
//            println("OUTCOME FOR: Y = ${youSkipValves}, E = ${elephantSkipValves}")
            val ypr = youStepsTaken.pressureRelease(26)
            val epr = elephantStepsTaken.pressureRelease(26)
            val tpr = ypr + epr
            if (i % 100 == 0) {
                println("total pressure release = $tpr")
            }
            outcomes.add(Triple(tpr, youSkipValves, elephantSkipValves))
        }
        println()

        val sortedOutcomes = outcomes.sortedBy { it.first }

        println("# OUTCOMES = ${outcomes.size}")
        println("BEST OUTCOME = ${sortedOutcomes.last().first}")
        println("... you covered valves ${allWorkingValves - sortedOutcomes.last().second}")
        println("... elephant covered valves ${allWorkingValves - sortedOutcomes.last().third}")

        return Unit
    }
}

fun main() {
    Day16.solve(test = false)
}