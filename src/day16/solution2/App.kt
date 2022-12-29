package day16.solution2

// Refactored original solution. Attempt to improve performance.

import common.Solution
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class Valve(val label: String, val flowRate: Int, val tunnels: Set<String>)

typealias ValveList = List<Valve>
typealias ValveGraph = Map<String, Valve>

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
typealias TraverseCacheMap = MutableMap<TraverseCacheKey, Int>

class TraverseValves(
    valves: ValveList,
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

    fun traverse(skipValves: Set<String> = setOf()): Int {
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
    ): Int {
//        println("== Minute $time: At valve ${valve.label} ==")

        if (openedValves.size == workingValves.size || time >= maxTime) {
            return 0
        }

        val timeRemaining = maxTime - time
        val cacheKey = Triple(valve.label, timeRemaining, openedValves.sorted())

        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]!!
        }

        val openValveResult = openValve(valve, time, openedValves)
        val moveToValveResult = moveToConnectedValves(valve, time, openedValves)
        val bestResult = max(openValveResult, moveToValveResult)

        cache[cacheKey] = bestResult

        return bestResult
    }

    fun openValve(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
    ): Int {
        if (valve.label in openedValves || valve.flowRate == 0 || valve.label in skipValves) {
            return 0
        }

        val nextTime = time + 1
        val nextOpenedValves = openedValves + valve.label
        val pressureRelease = (maxTime - nextTime) * valve.flowRate

        return pressureRelease + takeNextStep(valve, nextTime, nextOpenedValves,)
    }

    fun moveToConnectedValves(
        valve: Valve,
        time: Int,
        openedValves: Set<String>,
    ): Int {
        var bestOutcome = 0

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
            val outcome = takeNextStep(toValve, nextTime, openedValves)

            if (outcome > bestOutcome) {
                bestOutcome = outcome
            }
        }

        return bestOutcome
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
//        return traverser.traverse()
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
            val youResult = youTraverser.traverse(youSkipValves)
            val elephantResult = elephantTraverser.traverse(elephantSkipValves)
            val tpr = youResult + elephantResult
            println("Attempt $i: $tpr")
            if (tpr > bestOutcome.first) {
                bestOutcome = Triple(tpr, youSkipValves, elephantSkipValves)
            }
        }

        println("BEST OUTCOME = ${bestOutcome.first}")
        println("... you covered valves ${allWorkingValves - bestOutcome.second}")
        println("... elephant covered valves ${allWorkingValves - bestOutcome.third}")

        return bestOutcome.first
//        return Unit
    }
}

fun main() {
    Day16.solve(test = false)
}