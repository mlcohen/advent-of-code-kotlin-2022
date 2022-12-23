package day14.solution1

// Original solution used to solve the AoC day 14 puzzle. Warts and all.

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

enum class PlotType {
    SAND_EMITTER,
    ROCK,
    STILL_SAND,
    FALLING_SAND,
    VOID_SAND,
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

    fun setPath(from_: Point, to_: Point, value: T) {
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

object GridPrettyPrinter {
    fun print(grid: Grid<PlotType>) {
        val tl = grid.topLeft
        val br = grid.bottomRight
        for (y in tl.y..br.y) {
            val s = (tl.x..br.x).map { x ->
                when (grid[x, y]) {
                    PlotType.ROCK -> '#'
                    PlotType.STILL_SAND -> 'O'
                    PlotType.SAND_EMITTER -> '+'
                    PlotType.FALLING_SAND -> '*'
                    PlotType.VOID_SAND -> '~'
                    else -> '.'
                }
            }.joinToString(" ")
            println(s)
        }
    }
}

enum class SandUnitPourState {
    START,
    FALLING,
    AT_REST,
    VOID,
}

class PourSandUnitIterator(
    startPosition: Point,
    private val grid: Grid<PlotType>,
) {
    private var _currentPosition = startPosition
    private var _state = SandUnitPourState.START

    val position: Point get() = this._currentPosition
    val state: SandUnitPourState get() = this._state

    fun hasNext(): Boolean {
        return this._state == SandUnitPourState.START || this._state == SandUnitPourState.FALLING
    }

    fun next(): Pair<Point, SandUnitPourState> {
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

            if (nextPosition !in this.grid) {
                this._currentPosition = nextPosition
                this._state = SandUnitPourState.VOID
                return (this.position to this.state)
            }

            val plot = this.grid[nextPosition]
            val blockingPlot = plot == PlotType.STILL_SAND || plot == PlotType.ROCK

            if (!blockingPlot) {
                this._currentPosition = nextPosition
                this._state = SandUnitPourState.FALLING
                return (this.position to this.state)
            }
        }

        this._state = SandUnitPourState.AT_REST

        return (this.position to this.state)
    }
}

enum class PourSandSimulationState {
    RUNNING,
    FINISHED,
}

class PourSandSimulationIterator(
    private val sandEmittingPosition: Point,
    grid: Grid<PlotType>,
) {
    private var _state = PourSandSimulationState.RUNNING
    private var _grid = grid.copy()
    private var _currentPourSandUnitIterator: PourSandUnitIterator

    val grid: Grid<PlotType> get() = this._grid.copy()
    val state: PourSandSimulationState get() = this._state

    init {
        this._currentPourSandUnitIterator = PourSandUnitIterator(sandEmittingPosition, this._grid)
    }

    fun hasNext(): Boolean {
        return this._state != PourSandSimulationState.FINISHED
    }

    fun next(): Grid<PlotType> {
        if (!hasNext()) {
            return this._grid.copy()
        }

        while (this._currentPourSandUnitIterator.hasNext()) {
            this._currentPourSandUnitIterator.next()
        }

        when (this._currentPourSandUnitIterator.state) {
            SandUnitPourState.VOID -> {
                this._state = PourSandSimulationState.FINISHED
                this._grid[this._currentPourSandUnitIterator.position] = PlotType.VOID_SAND
            }
            SandUnitPourState.AT_REST -> {
                this._grid[this._currentPourSandUnitIterator.position] = PlotType.STILL_SAND
                this._currentPourSandUnitIterator = PourSandUnitIterator(this.sandEmittingPosition, this._grid)
            }
            else -> throw error("Pour sand unit iterator in unexpected state ${this._currentPourSandUnitIterator.state}")
        }

        return this._grid.copy()
    }
}

object Day14 : Solution.LinedInput<Grid<PlotType>>(day = 14) {

    override fun parseInput(input: List<String>): Grid<PlotType> {
        val segments = input.map{ line ->
            line.split(" -> ").map {
                it.split(',').let { (a, b) -> Point(a.toInt(), b.toInt()) }
            }
        }

        val grid = Grid<PlotType>()
        grid[500, 0] = PlotType.SAND_EMITTER

        segments.forEach { points ->
            points.windowed(2) { (a, b) -> grid.setPath(a, b, PlotType.ROCK) }
        }

        return grid
    }

    override fun part1(grid: Grid<PlotType>): Any {
        val sandEmitterPoint = Point(500, 0)
        val iter = PourSandSimulationIterator(sandEmitterPoint, grid)

        while (iter.hasNext()) {
            iter.next()
        }

        GridPrettyPrinter.print(iter.grid)
        println()

        return iter.grid.toMap().count { (_, plotType) -> plotType == PlotType.STILL_SAND }
    }

    override fun part2(grid: Grid<PlotType>): Any {
        return Unit
    }
}

fun main() {
    Day14.solve(test = false)
}