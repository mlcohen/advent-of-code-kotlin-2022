package day14.solution1

// Original solution used to solve the AoC day 14 puzzle.

import common.Solution
import kotlin.math.max
import kotlin.math.min

data class Offset(val x: Int, val y: Int) {
    operator fun plus(offset: Offset): Offset {
        return Offset(this.x + offset.x, this.y + offset.y)
    }

    operator fun times(value: Int): Offset {
        return Offset(this.x * value, this.y * value)
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

    operator fun times(value: Int): Offset {
        return this.offset * value
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

interface GridExtension<T> {
    fun prepare(grid: Grid<T>);
    fun get(p: Point, grid: Grid<T>): T?;
    fun pointIsEmpty(p: Point, grid: Grid<T>): Boolean;
    fun contains(p: Point, grid: Grid<T>): Boolean;
}

class Grid<T>(
    private val data: MutableMap<Point, T> = mutableMapOf(),
    extension: GridExtension<T>? = null,
) {
    private var _extension: GridExtension<T>? = extension
    private var _topLeft: Point = Point(0, 0)
    private var _bottomRight: Point = Point(0, 0)

    var extension: GridExtension<T>?
        get() = this._extension
        set(value) {
            this._extension = value
            value?.prepare(this)
        }

    val topLeft: Point get() = this._topLeft
    val bottomRight: Point get() = this._bottomRight

    operator fun set(p: Point, value: T) {
        val pointIsEmptyInExtension = this.extension?.pointIsEmpty(p, this) ?: true

        if (pointIsEmptyInExtension) {
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
    }

    operator fun set(x: Int, y: Int, value: T) {
        this[Point(x, y)] = value
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
        if (data.containsKey(p)) {
            return data[p]
        }

        return this.extension?.get(p, this)
    }

    operator fun get(x: Int, y: Int): T? {
        return this[Point(x, y)]
    }

    operator fun contains(p: Point): Boolean {
        val tl = this.topLeft
        val br = this.bottomRight
        val contained = (p.x >= tl.x && p.x <= br.x) && (p.y <= br.y && p.y >= tl.y)

        if (!contained) {
            return this.extension?.contains(p, this) ?: false
        }

        return true
    }

    fun toMap(): Map<Point, T> {
        return this.data.toMap()
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
    private var _cave = cave
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
            return this._cave
        }

        while (this._currentPourSandUnitIterator.hasNext()) {
            this._currentPourSandUnitIterator.next()
        }

        when (this._currentPourSandUnitIterator.state) {
            PourSandUnitState.VOID -> {
                this._state = PourSandSimulationState.FINISHED
            }
            PourSandUnitState.AT_REST -> {
                if (this._currentPourSandUnitIterator.position == sandEmitterPosition) {
                    this._state = PourSandSimulationState.FINISHED
                } else {
                    this._cave[this._currentPourSandUnitIterator.position] = CavePlotType.SETTLED_SAND
                    this._currentPourSandUnitIterator = PourSandUnitIterator(this.sandEmitterPosition, this._cave)
                }
            }
            else -> throw error("Pour sand unit iterator in unexpected state ${this._currentPourSandUnitIterator.state}")
        }

        return this._cave
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
        val tl = cave.topLeft + (Direction.LEFT * 6)
        val br = cave.bottomRight + (Direction.DOWN * 2) + (Direction.RIGHT * 6)
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

class InfiniteFloorCaveExtension(
    private val floorOffset: Int = 2,
): GridExtension<CavePlotType> {
    private var floorY: Int = 0

    override fun prepare(grid: Grid<CavePlotType>) {
        val lowestRockPos = grid.toMap().filter { it.value == CavePlotType.ROCK }.maxBy { it.key.y }.let { it.key }
        this.floorY = lowestRockPos.y + floorOffset
    }

    override fun get(p: Point, grid: Grid<CavePlotType>): CavePlotType? {
        return if (p.y == this.floorY) {
            CavePlotType.ROCK
        } else null
    }

    override fun contains(p: Point, grid: Grid<CavePlotType>): Boolean {
        return p.y <= this.floorY
    }

    override fun pointIsEmpty(p: Point, grid: Grid<CavePlotType>): Boolean {
        return p.y != this.floorY
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

//        CavePrettyPrinter.print(finalCave)
//        println()

        return finalCave.toMap().count { (_, plotType) -> plotType == CavePlotType.SETTLED_SAND }
        return Unit
    }

    override fun part2(initCave: Cave): Any {
        val sandEmitterPosition = Point(500, 0)
        initCave[sandEmitterPosition] = CavePlotType.SAND_EMITTER
        initCave.extension = InfiniteFloorCaveExtension()

        val simulator = PourSandSimulator(sandEmitterPosition, initCave)
        val finalCave = simulator.run()
        finalCave[sandEmitterPosition] = CavePlotType.SETTLED_SAND

//        CavePrettyPrinter.print(finalCave)
//        println()

        return finalCave.toMap().count { (_, plotType) -> plotType == CavePlotType.SETTLED_SAND }

    }
}

fun main() {
    Day14.solve(test = false)
}