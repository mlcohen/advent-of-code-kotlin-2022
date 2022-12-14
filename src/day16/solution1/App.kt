package day16.solution1 

// Original solution used to solve the AoC day 16 puzzle

import common.Solution
import kotlin.math.min
import kotlin.math.pow

data class Valve(val label: String, val flowRate: Int, val tunnels: Set<String>)

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

fun shortestPaths(g: ValveGraph): Map<String, Map<String, Int>> {
    val INF = Int.MAX_VALUE
    val dist = g.keys.map { p ->
        g[p]!!.tunnels.map { q -> p to q }
    }.flatten().associateWith { 1 }.toMutableMap()
    val nodes = g.keys

    for (r in nodes) {
        for (p in nodes) {
            for (q in nodes) {
                val dpq = dist[p to q] ?: INF
                val dpr = dist[p to r] ?: INF
                val drq = dist[r to q] ?: INF
                val dprq = if (dpr == INF || drq == INF) INF else dpr + drq
                dist[p to q] = min(dpq, dprq)
            }
        }
    }

    return dist
        .filter { it.value != INF }
        .filter { it.key.first != it.key.second }
        .toList()
        .groupBy { (k, _) -> k.first }
        .mapValues { (_, v) -> v.associate { it.first.second to it.second } }
        .toMap()
}

typealias TraverseCacheKey = Triple<String, Int, List<String>>
typealias TraverseCacheMap = MutableMap<TraverseCacheKey, StepsTaken>

class TraverseValves(
    private val valves: ValveList,
    private val maxTime: Int,
) {
    private val graph: ValveGraph = valves.associateBy { it.label }
    private val workingValves: Set<String> = valves.filter { it.flowRate > 0 }.map { it.label }.toSet()
    private val nodes: Set<String> = this.workingValves + "AA"
    private val edges: Map<String, Map<String, Int>> = shortestPaths(graph)
        .filter { (k) -> k in this.nodes }
        .mapValues { (_, v) -> v.filter { it.key in this.nodes } }
    private var skipValves: Set<String> = setOf()
    private var cache: TraverseCacheMap = mutableMapOf()

    fun traverse(skipValves: Set<String> = setOf()): StepsTaken {
        this.skipValves = skipValves
        this.cache = mutableMapOf()
        val openedValves = setOf<String>()
        val result = takeNextStep(graph["AA"]!!, 0 , openedValves)
        return result
    }

    fun takeNextStep(
        valve: Valve,
        time: Int = 0,
        openedValves: Set<String>,
    ): StepsTaken {
//        println("== Minute $time: At valve ${valve.label} ==")

        if (openedValves.size == workingValves.size || time >= maxTime) {
            return listOf()
        }

        val timeRemaining = maxTime - time
        val cacheKey = Triple(valve.label, timeRemaining, openedValves.sorted())

        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]!!
        }

        val openValveResult = openValve(valve, time, openedValves)
        val moveToValveResult = moveToConnectedValves(valve, time, openedValves)
        val bestResult = listOf(openValveResult, moveToValveResult).maxBy { it.pressureRelease(maxTime) }

        cache[cacheKey] = bestResult

        return bestResult
    }

    fun openValve(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
    ): StepsTaken {
        if (valve.label in openedValves || valve.flowRate == 0 || valve.label in skipValves) {
            return listOf()
        }
//        println("You open valve ${valve.label}")
        val nextTime = time + 1
        val nextOpenedValves = openedValves + valve.label
        val stepTaken = StepTaken(Action.OPENED_VALVE, valve, nextTime)

        return listOf(stepTaken) + takeNextStep(valve, nextTime, nextOpenedValves,)
    }

    fun moveToConnectedValves(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
    ): StepsTaken {
        val outcomes = mutableListOf<StepsTaken>()

        for (edge in this.edges[valve.label]!!) {
            val (toValveLabel, distance) = edge
            val nextTime = time + distance

            if (toValveLabel in skipValves) {
                continue
            }

            if (nextTime >= this.maxTime) {
                continue
            }

            val toValve = graph[toValveLabel]!!
            val stepTaken = StepTaken(Action.MOVED_TO_VALVE, toValve, nextTime)
            val outcome = listOf(stepTaken) + takeNextStep(toValve, nextTime, openedValves)

            outcomes.add(outcome)
        }

        if (outcomes.isEmpty()) {
            return listOf()
        }

        return outcomes.maxBy { it.pressureRelease(maxTime) }
    }

}

object Day16 : Solution.LinedInput<ValveList>(day = 16) {

    override fun parseInput(input: List<String>): ValveList {
        return input.map{ line ->
            line.split(';').let { (a, b) ->
                val left = a.split(' ', '=')
                    .let { tokens -> (tokens[1] to tokens.last().toInt()) }
                val right = b.trim().split(' ', ',')
                    .let { tokens -> tokens.drop(4).filter(String::isNotEmpty).toSet() }
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

        val youTraverser = TraverseValves(input, 26)
        val elephantTraverser = TraverseValves(input, 26)

        var bestOutcome: Triple<Int, Set<String>, Set<String>> = Triple(0, setOf(), setOf())
        val maxPermuations = 2.0.pow(allWorkingValves.size).toInt() / 2

        for (i in 1 until maxPermuations) {
            val skipValves = filterValves(i)
            val youSkipValves = skipValves
            val elephantSkipValves = allWorkingValves.toSet() - skipValves
            val youStepsTaken = youTraverser.traverse(youSkipValves)
            val elephantStepsTaken = elephantTraverser.traverse(elephantSkipValves)
            val ypr = youStepsTaken.pressureRelease(26)
            val epr = elephantStepsTaken.pressureRelease(26)
            val tpr = ypr + epr

            if (tpr > bestOutcome.first) {
                bestOutcome = Triple(tpr, youSkipValves, elephantSkipValves)
            }
        }

        println("BEST OUTCOME = ${bestOutcome.first}")
        println("... you covered valves ${allWorkingValves - bestOutcome.second}")
        println("... elephant covered valves ${allWorkingValves - bestOutcome.third}")

        return bestOutcome.first
    }
}

fun main() {
    Day16.solve(test = true)
}