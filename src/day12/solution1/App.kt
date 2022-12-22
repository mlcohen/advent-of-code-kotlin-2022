package day12.solution1

// Original solution used to solve the AoC day 12 puzzle. Warts and all.

import common.Solution
import kotlin.math.abs

val HEIGHT_VALUES = ('a'..'z').toList().associate { (it to (it.code - 'a'.code + 1)) }

data class Offset(val row: Int, val col: Int)
data class Point(val row: Int, val col: Int) {
    operator fun plus(offset: Offset): Point {
        return Point(this.row + offset.row, this.col + offset.col)
    }
    operator fun plus(direction: Direction): Point {
        return this + direction.offset
    }
    fun manhattanDistanceTo(p: Point): Int {
        val rowLength = abs(this.row - p.row)
        val colLength = abs(this.col - p.col)
        return rowLength + colLength
    }
    override fun toString(): String = "(${row}, ${col})"
    companion object {
        val ZERO: Point = Point(0, 0)
    }
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
    operator fun get(p: Point): Int  = this.values[p.row][p.col]
    operator fun get(row: Int, col: Int): Int  = this.values[row][col]
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

object TraverseGrid {
    fun findMinPath(heightGrid: Grid, startPos: Point, endPos: Point): Path {
        fun h(p: Point): Int = p.manhattanDistanceTo(endPos)

        var openNodes = mutableSetOf(startPos)
        val cameFrom = mutableMapOf<Point, Point>()
        val gScore = mutableMapOf(startPos to 0)
        val fScore = mutableMapOf(startPos to h(startPos))

        fun reconstructPath(current: Point): Path {
            val path = mutableListOf(current)
            var next = current
            while (next in cameFrom) {
                next = cameFrom[next]!!
                path.add(next)
            }
            return path.toList().reversed()
        }

        while (openNodes.isNotEmpty()) {
            val current = openNodes.minByOrNull { fScore[it]!! }!!

            if (current == endPos) {
                return reconstructPath(current)
            }

            openNodes.remove(current)

            for (direction in Direction.values()) {
                val neighbour = current + direction

                if (neighbour !in heightGrid) {
                    continue
                }

                val heightDiff = heightGrid[neighbour] - heightGrid[current]

                if (heightDiff > 1) {
                    continue
                }

                val stepCost = when (heightDiff) {
                    1 -> 1
                    0 -> 2
                    else -> 3
                }

                val tentativeGScore = gScore[current]?.let { it + stepCost } ?: Int.MAX_VALUE
                val neighborGScore = gScore[neighbour] ?: Int.MAX_VALUE

                if (tentativeGScore < neighborGScore) {
                    cameFrom[neighbour] = current
                    gScore[neighbour] = tentativeGScore
                    fScore[neighbour] = tentativeGScore + h(neighbour)
                    if (neighbour !in openNodes) {
                        openNodes += neighbour
                    }
                }
            }
        }

        return listOf()
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
        val result = TraverseGrid.findMinPath(grid, startPos, endPos)
        return result.size - 1
    }

    override fun part2(input: ParsedInput): Any {
        return Unit
    }
}

fun main() {
    Day12.solve(test = false)
}