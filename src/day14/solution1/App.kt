package day14.solution1

// Original solution used to solve the AoC day 14 puzzle.

import common.Solution
import kotlin.math.max
import kotlin.math.min

data class Offset(val x: Int, val y: Int) {
    operator fun plus(offset: Offset): Offset {
        return Offset(this.x + offset.x, this.y + offset.y)
    }
}

enum class Direction(val offset: Offset) {
    UP(Offset(0, -1)),
    DOWN(Offset(0, 1)),
    LEFT(Offset(-1, 0)),
    RIGHT(Offset(1, 0));

    operator fun plus(direction: Direction): Offset {
        return this.offset + direction.offset
    }
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(offset: Offset): Point {
        return Point(this.x + offset.x, this.y + offset.y)
    }
    operator fun plus(direction: Direction): Point {
        return this + direction.offset
    }
    override fun toString(): String = "($x, $y)"
}

class Grid<T>(
    private val data: MutableMap<Point, T> = mutableMapOf(),
) {
    val topLeft: Point get() {
        var minX = -1
        var minY = -1
        for (p in data.keys) {
            minX = if (minX < 0) p.x else min(minX, p.x)
            minY = if (minY < 0) p.y else min(minY, p.y)
        }
        return Point(minX, minY)
    }

    val bottomRight: Point get() {
        var maxX = -1
        var maxY = -1
        for (p in data.keys) {
            maxX = if (maxX < 0) p.x else max(maxX, p.x)
            maxY = if (maxY < 0) p.y else max(maxY, p.y)
        }
        return Point(maxX, maxY)
    }

    operator fun set(p: Point, value: T) {
        data[p] = value
    }

    operator fun set(x: Int, y: Int, value: T) {
        data[Point(x, y)] = value
    }

    fun setPoints(pts: List<Point>, value: T) {
        pts.forEach { this[it] = value }
    }

    fun setLineSegment(from_: Point, to_: Point, value: T) {
        if (from_.x == to_.x) {
            for (y in min(from_.y, to_.y)..max(from_.y, to_.y)) {
                this[from_.x, y] = value
            }
        }
        if (from_.y == to_.y) {
            for (x in min(from_.x, to_.x)..max(from_.x, to_.x)) {
                this[x, from_.y] = value
            }
        }
    }

    operator fun get(p: Point): T? {
        return data[p]
    }

    operator fun get(x: Int, y: Int): T? {
        return data[Point(x, y)]
    }

    fun pointAssigned(p: Point): Boolean {
        return data.containsKey(p)
    }

    fun pointAssigned(x: Int, y: Int): Boolean {
        return pointAssigned(Point(x, y))
    }

    fun pointNotAssigned(p: Point): Boolean {
        return !pointAssigned(p)
    }

    fun pointNotAssigned(x: Int, y: Int): Boolean {
        return !pointAssigned(x, y)
    }

    operator fun contains(p: Point): Boolean {
        val tl = this.topLeft
        val br = this.bottomRight
        return (p.x >= tl.x && p.x <= br.x) && (p.y <= br.y && p.y >= tl.y)
    }

    fun forEach(fn: (key: Point, value: T) -> Unit) {
        this.data.forEach(fn)
    }

    fun toMap(): Map<Point, T> {
        return this.data.toMap()
    }

    fun copy(): Grid<T> {
        return Grid(this.data.toMutableMap())
    }
}

enum class CavePlotType {
    ROCK,
    FALLING_SAND,
    SETTLED_SAND,
    VOID_SAND,
    SAND_EMITTER,
}

typealias Cave = Grid<CavePlotType>

enum class PourSandUnitState {
    FALLING,
    AT_REST,
    VOID,
}

class PourSandUnitIterator(
    val sandEmitterPosition: Point,
    private val cave: Cave,
) {
    private var _currentPosition = sandEmitterPosition
    private var _state = PourSandUnitState.FALLING

    val position: Point get() = this._currentPosition
    val state: PourSandUnitState get() = this._state

    fun hasNext(): Boolean {
        return this._state == PourSandUnitState.FALLING
    }

    fun next(): Pair<Point, PourSandUnitState> {
        if (!this.hasNext()) {
            return (this.position to this.state)
        }

        val offsets = listOf(
            Direction.DOWN.offset,
            Direction.DOWN + Direction.LEFT,
            Direction.DOWN + Direction.RIGHT,
        )

        for (offset in offsets) {
            val nextPosition = this.position + offset

            if (nextPosition !in this.cave) {
                this._currentPosition = nextPosition
                this._state = PourSandUnitState.VOID
                return (this.position to this.state)
            }

            val plot = this.cave[nextPosition]
            val blockingPlot = plot == CavePlotType.SETTLED_SAND || plot == CavePlotType.ROCK

            if (!blockingPlot) {
                this._currentPosition = nextPosition
                this._state = PourSandUnitState.FALLING
                return (this.position to this.state)
            }
        }

        this._state = PourSandUnitState.AT_REST

        return (this.position to this.state)
    }

    fun collectPath(): List<Point> {
        val path = mutableListOf<Point>()
        while (this.hasNext()) {
            val (point) = this.next()
            path.add(point)
        }
        return path.toList()
    }
}

enum class PourSandSimulationState {
    RUNNING,
    FINISHED,
}

class PourSandSimulationIterator(
    val sandEmitterPosition: Point,
    cave: Cave,
) {
    private var _state = PourSandSimulationState.RUNNING
    private var _cave = cave.copy()
    private var _currentPourSandUnitIterator: PourSandUnitIterator

    val cave: Cave get() = this._cave
    val state: PourSandSimulationState get() = this._state

    init {
        this._currentPourSandUnitIterator = PourSandUnitIterator(sandEmitterPosition, this._cave)
    }

    fun hasNext(): Boolean {
        return this._state != PourSandSimulationState.FINISHED
    }

    fun next(): Cave {
        if (!hasNext()) {
            return this._cave.copy()
        }

        while (this._currentPourSandUnitIterator.hasNext()) {
            this._currentPourSandUnitIterator.next()
        }

        when (this._currentPourSandUnitIterator.state) {
            PourSandUnitState.VOID -> {
                this._state = PourSandSimulationState.FINISHED
            }
            PourSandUnitState.AT_REST -> {
                this._cave[this._currentPourSandUnitIterator.position] = CavePlotType.SETTLED_SAND
                this._currentPourSandUnitIterator = PourSandUnitIterator(this.sandEmitterPosition, this._cave)
            }
            else -> throw error("Pour sand unit iterator in unexpected state ${this._currentPourSandUnitIterator.state}")
        }

        return this._cave.copy()
    }
}

class PourSandSimulator(
    private val sandEmitterPosition: Point,
    private val cave: Cave,
) {
    fun run(): Cave {
        val iter = PourSandSimulationIterator(sandEmitterPosition, cave)

        while (iter.hasNext()) {
            iter.next()
        }

        return iter.cave
    }
}

object CavePrettyPrinter {
    fun print(cave: Cave) {
        val tl = cave.topLeft
        val br = cave.bottomRight
        for (y in tl.y..br.y) {
            val s = (tl.x..br.x).map { x ->
                when (cave[x, y]) {
                    CavePlotType.ROCK -> '#'
                    CavePlotType.SETTLED_SAND -> 'O'
                    CavePlotType.SAND_EMITTER -> '+'
                    CavePlotType.FALLING_SAND -> '*'
                    CavePlotType.VOID_SAND -> '~'
                    else -> '.'
                }
            }.joinToString("")
            println(s)
        }
    }
}

object Day14 : Solution.LinedInput<Cave>(day = 14) {

    override fun parseInput(input: List<String>): Cave {
        val segments = input.map{ line ->
            line.split(" -> ").map {
                it.split(',').let { (a, b) -> Point(a.toInt(), b.toInt()) }
            }
        }

        val cave = Cave()

        segments.forEach { points ->
            points.windowed(2) { (a, b) -> cave.setLineSegment(a, b, CavePlotType.ROCK) }
        }

        return cave
    }

    override fun part1(initCave: Cave): Any {
        val sandEmitterPosition = Point(500, 0)
        initCave[sandEmitterPosition] = CavePlotType.SAND_EMITTER
        val simulator = PourSandSimulator(sandEmitterPosition, initCave)
        val finalCave = simulator.run()

        val sandUnitIter = PourSandUnitIterator(sandEmitterPosition, finalCave)
        val path = sandUnitIter.collectPath()
        finalCave.setPoints(path, CavePlotType.VOID_SAND)

        CavePrettyPrinter.print(finalCave)
        println()

        return finalCave.toMap().count { (_, plotType) -> plotType == CavePlotType.SETTLED_SAND }
    }

    override fun part2(cave: Cave): Any {
        return Unit
    }
}

fun main() {
    Day14.solve(test = false)
}