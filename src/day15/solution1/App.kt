package day15.solution1

// Original solution used to solve the AoC day 15 puzzle

import common.Solution
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Point(val x: Int, val y: Int) {
    fun manhanttanDistanceTo(v: Point): Int {
        val dx = abs(this.x - v.x)
        val dy = abs(this.y - v.y)
        return dx + dy
    }
    override fun toString(): String = "($x, $y)"
}

fun IntRange.overlaps(other: IntRange): Boolean {
    return other.first in this || other.last in this
}

fun IntRange.contiguousWith(other: IntRange): Boolean {
    return this.overlaps(other) || other.first - this.last == 1 || this.first - other.last == 1
}

fun IntRange.union(other: IntRange): IntRange {
    return min(this.first, other.first)..max(this.last, other.last)
}

fun List<IntRange>.union(): List<IntRange> {
    if (this.isEmpty()) {
        return listOf()
    }

    val unionedRanges = mutableListOf<IntRange>()
    val sortedRanges = this.sortedBy { it.first }
    var current = sortedRanges[0]

    for (r in sortedRanges.drop(1)) {
        if (current.contiguousWith(r)) {
            current = current.union(r)
        } else {
            unionedRanges.add(current)
            current = r
        }
    }

    unionedRanges.add(current)

    return unionedRanges.toList()
}

fun List<IntRange>.contains(value: Int): Boolean {
    for (r in this) {
        if (value in r) {
            return true
        }
    }
    return false
}

typealias Beacon = Point
typealias Sensor = Point
typealias SensorBeaconPair = Pair<Sensor, Beacon>
typealias SensorBeaconPairList = List<Pair<Sensor, Beacon>>

fun SensorBeaconPair.horizontalRangeCoverage(y: Int): IntRange? {
    val sensor = this.first
    val beacon = this.second
    val md = sensor.manhanttanDistanceTo(beacon)
    val minY = sensor.y - md
    val maxY = sensor.y + md

    if (y !in minY..maxY) {
        return null
    }

    val xOffset = md - abs(y - sensor.y)
    val minX = sensor.x - xOffset
    val maxX = sensor.x + xOffset

    return minX..maxX
}

fun SensorBeaconPair.coverage(): Set<Point> {
    val sensor = this.first
    val beacon = this.second
    val md = sensor.manhanttanDistanceTo(beacon)
    val minY = sensor.y - md
    val maxY = sensor.y + md
    val pts = mutableSetOf<Point>()
    for (y in minY..maxY) {
        val xOffset = md - abs(y - sensor.y)
        val minX = sensor.x - xOffset
        val maxX = sensor.x + xOffset
        for (x in minX..maxX) {
            pts += Point(x, y)
        }
    }
    return pts.toSet()
}

fun SensorBeaconPairList.horizontalRangeCoverage(y: Int): List<IntRange> {
    return this.mapNotNull { it.horizontalRangeCoverage(y) }.union()
}

class Terrain<T>(
    private val data: MutableMap<Point, T> = mutableMapOf(),
) {
    private var _topLeft: Point = Point(0, 0)
    private var _bottomRight: Point = Point(0, 0)

    val topLeft: Point get() = this._topLeft
    val bottomRight: Point get() = this._bottomRight

    operator fun set(p: Point, value: T) {
        if (this.data.isEmpty()) {
            this._topLeft = p
            this._bottomRight = p
        } else {
            val minX = min(p.x, this._topLeft.x)
            val minY = min(p.y, this._topLeft.y)
            val maxX = max(p.x, this._bottomRight.x)
            val maxY = max(p.y, this._bottomRight.y)

            this._topLeft = Point(minX, minY)
            this._bottomRight = Point(maxX, maxY)
        }

        this.data[p] = value
    }

    operator fun set(x: Int, y: Int, value: T) {
        this[Point(x, y)] = value
    }

    operator fun get(p: Point): T? {
        return this.data[p]
    }

    operator fun get(x: Int, y: Int): T? {
        return this[Point(x, y)]
    }
}

enum class TerrainEntry {
    SENSOR,
    BEACON,
    RANGE,
    DISTESS_SIGNAL,
}

typealias SenorTerrain = Terrain<TerrainEntry>

fun SenorTerrain.add(entries: List<SensorBeaconPair>) {
    entries.forEach {
        this[it.first] = TerrainEntry.SENSOR
        this[it.second] = TerrainEntry.BEACON
    }
}

object SenorTerrainPrettyPrinter {

    fun print(input: SensorBeaconPairList, fn: ((SenorTerrain) -> Unit)? = null) {
        val terrain = SenorTerrain()

        var fullCoverage = setOf<Point>()
        input.forEach { pair ->
            val coverage = pair.coverage()
            fullCoverage = fullCoverage.union(coverage)
        }

        fullCoverage.forEach { terrain[it] = TerrainEntry.RANGE }
        terrain.add(input)

        fn?.let { fn(terrain) }

        this.print(terrain)
    }

    fun print(terrain: SenorTerrain) {
        val tl = terrain.topLeft
        val br = terrain.bottomRight
        val maxYStringLength = max(abs(tl.y), abs(br.y)).toString().length.let {
            it + if (tl.y < 0) 1 else 0
        }
        for (y in tl.y..br.y) {
            val rowPlot = (tl.x..br.x).map { x ->
                when (terrain[x, y]) {
                    TerrainEntry.BEACON -> 'B'
                    TerrainEntry.SENSOR -> 'S'
                    TerrainEntry.RANGE -> '#'
                    TerrainEntry.DISTESS_SIGNAL -> '+'
                    else -> '.'
                }
            }.joinToString("")
            val header = "$y".padStart(maxYStringLength, ' ')
            println("$header $rowPlot")
        }
    }
}



object Day15 : Solution.LinedInput<SensorBeaconPairList>(day = 15) {

    override fun parseInput(input: List<String>): SensorBeaconPairList {
        val r = input.map{ line ->
            """-?\d+""".toRegex()
                .findAll(line)
                .toList()
                .map { m -> m.groups.map { g -> g!!.value.toInt() } }
                .flatten()
                .let { (a, b, c, d) -> Sensor(a, b) to Beacon(c, d)  }
        }
        return r
    }

    override fun part1(input: SensorBeaconPairList): Any {
        val y = 10
        val rowCoverage = input.horizontalRangeCoverage(y)
        val beacons = input
            .map { it.second }
            .filter { it.y == y }
            .filter { rowCoverage.contains(it.y) }
            .toSet()
        val plotPointCount = rowCoverage.sumOf { it.last - it.first + 1 }

        return plotPointCount - beacons.size
    }

    override fun part2(input: SensorBeaconPairList): Any {
//        SenorTerrainPrettyPrinter.print(input) { terrain ->
//            terrain[Point(14, 11)] = TerrainEntry.DISTESS_SIGNAL
//        }
        val bottomMostSensor = input.maxBy { it.first.y }
        val maxY = bottomMostSensor.first.y

        var distressRowCoverage: Pair<Int, List<IntRange>>? = null

        for (y in 0..maxY) {
            val rowCoverage = input.horizontalRangeCoverage(y)
            if (rowCoverage.size > 1) {
                distressRowCoverage = (y to rowCoverage)
                break
            }
        }

        val distressYPos = distressRowCoverage!!.first
        val distressXPos = distressRowCoverage!!.second.first().last + 1

        return (distressXPos.toLong() * 4000000) + distressYPos.toLong()
    }
}

fun main() {
    Day15.solve(test = true)
}