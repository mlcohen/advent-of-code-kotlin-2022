package day12

// Original solution used to solve the AoC day 12 puzzle. Warts and all.

import common.Solution

val HEIGHT_VALUES = ('a'..'z').toList().associate { (it to (it.code - 'a'.code + 1)) }

data class Offset(val row: Int, val col: Int)
data class Point(val row: Int, val col: Int) {
    operator fun plus(offset: Offset): Point {
        return Point(this.row + offset.row, this.col + offset.col)
    }
    override fun toString(): String = "(${row}, ${col})"
}

enum class Direction(val offset: Offset) {
    UP(Offset(-1, 0)),
    DOWN(Offset(1, 0)),
    LEFT(Offset(0, -1)),
    RIGHT(Offset(0, 1)),
}

data class Grid(val values: List<List<Int>>) {
    val width: Int get() = this.values.first().size
    val height: Int get() = this.values.size
    fun map(fn: (row: Int, col: Int, value: Int) -> Int): Grid {
        val values = this.values.mapIndexed { rowIdx, row ->
            row.mapIndexed { colIdx, value ->  fn(rowIdx, colIdx, value) }
        }
        return Grid(values = values)
    }
    operator fun get(p: Point): Int  = this.values[p.row][p.col]
    operator fun get(row: Int, col: Int): Int  = this.values[row][col]
    operator fun set(p: Point, value: Int): Unit {
        this.map { r, c, v -> if (p.row == r && p.col == c) value else v }
    }
    operator fun set(row: Int, col: Int, value: Int): Unit {
        this.map { r, c, v -> if (row == r && col == c) value else v }
    }
    operator fun contains(p: Point): Boolean
        = (p.col in 0 until this.width) && (p.row in 0 until this.height)
    fun prettyPrint() {
        this.values.forEach { line ->
            val s = line.joinToString(" ") { it.toString().padStart(2, ' ') }
            println(s)
        }
    }
}

typealias MutableGrid = MutableList<MutableList<Int>>

fun MutableGrid.toGrid(): Grid {
    val values = this.map { it.toList() }.toList()
    return Grid(values)
}

fun createMutableGrid(width: Int, height: Int): MutableGrid {
    return MutableList(height) { MutableList(width) { 0 } }
}

typealias Path = List<Point>
typealias TravelCost = Int
typealias FindMinPathResult = Pair<Path, TravelCost>

object TraverseGrid {
    fun findMinPath(heightGrid: Grid, startPos: Point, endPos: Point): Path {
        val stepToCache = mutableMapOf<Point, FindMinPathResult>()

        fun stepTo(
            p: Point,
            travelledPoints: Set<Point>,
            indent: Int = 0
        ): FindMinPathResult {
            if (p == endPos) {
                return (listOf(endPos) to 0)
            }

            if (p in stepToCache) {
                val cachedResult = stepToCache[p]!!
                return stepToCache[p]!!
            }

            var minPath: Path = listOf()
            var minTravelCost: TravelCost = -1

            for (direction in Direction.values()) {
                val nextPoint = p + direction.offset

                if (nextPoint !in heightGrid || nextPoint in travelledPoints) {
                    continue
                }

                val heightDiff = heightGrid[nextPoint] - heightGrid[p]

                if (heightDiff > 1) {
                    continue
                }

                val stepCost = when (heightDiff) {
                    1 -> 2
                    else -> 1
                }

                val (path, travelCost) = stepTo(nextPoint, travelledPoints + nextPoint, indent + 4)
                val thisPath = listOf(p) + path
                val thisTravelCost = travelCost + stepCost
                if (thisPath.last() != endPos || travelCost == -1) {
                    continue
                }

                if (minTravelCost == -1 || thisTravelCost < minTravelCost) {
                    minPath = thisPath
                    minTravelCost = thisTravelCost
                }
            }

            val result = (minPath to minTravelCost)

            stepToCache[p] = result

            return (minPath to minTravelCost)
        }

        return stepTo(startPos, setOf(startPos)).first
    }
}

typealias ParsedInput = Triple<Grid, Point, Point>

object Day12 : Solution.LinedInput<ParsedInput>(day = 12) {

    override fun parseInput(input: List<String>): ParsedInput {
        var startPos = Point(0, 0)
        var endPos = Point(0, 0)
        val grid = createMutableGrid(width = input.first().length, height = input.size)

        input.forEachIndexed { rowIdx, line ->
            line.forEachIndexed { colIdx, ch -> when (ch) {
                'S' -> {
                    startPos = Point(rowIdx, colIdx)
                    grid[rowIdx][colIdx] = HEIGHT_VALUES['a']!!
                }
                'E' -> {
                    endPos = Point(rowIdx, colIdx)
                    grid[rowIdx][colIdx] = HEIGHT_VALUES['z']!!
                }
                else -> grid[rowIdx][colIdx] = HEIGHT_VALUES[ch]!!
            } }
        }

        return Triple(grid.toGrid(), startPos, endPos)
    }

    override fun part1(input: ParsedInput): Any {
        val (grid, startPos, endPos) = input
        println(startPos)
        println(endPos)
        grid.prettyPrint()
        val result = TraverseGrid.findMinPath(grid, startPos, endPos)
        println(result)
        println(result.size)

        return Unit
    }

    override fun part2(input: ParsedInput): Any {
        return Unit
    }
}

fun main() {
    Day12.solve(test = false)
}