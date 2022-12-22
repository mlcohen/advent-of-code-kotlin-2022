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
}

enum class Direction(val offset: Offset) {
    UP(Offset(-1, 0)),
    DOWN(Offset(1, 0)),
    LEFT(Offset(0, -1)),
    RIGHT(Offset(0, 1)),
}

typealias ElevationMap = Map<Point, Char>
typealias Path = List<Point>

object TraverseGrid {
    fun findMinPath(elevations: ElevationMap, startPos: Point, endPos: Point): Path {
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

                if (neighbour !in elevations) {
                    continue
                }

                val elevationCurrent = HEIGHT_VALUES[elevations[current]]!!
                val elevationNeighbor = HEIGHT_VALUES[elevations[neighbour]]!!
                val elevationDiff = elevationNeighbor - elevationCurrent

                if (elevationDiff > 1) {
                    continue
                }

                val stepCost = when (elevationDiff) {
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

typealias ParsedInput = Triple<ElevationMap, Point, Point>

object Day12 : Solution.LinedInput<ParsedInput>(day = 12) {

    override fun parseInput(input: List<String>): ParsedInput {
        var startPos = Point(0, 0)
        var endPos = Point(0, 0)
        val elevations = mutableMapOf<Point, Char>()

        input.forEachIndexed { rowIdx, line ->
            line.forEachIndexed { colIdx, ch -> when (ch) {
                'S' -> {
                    startPos = Point(rowIdx, colIdx)
                    elevations[startPos] = 'a'
                }
                'E' -> {
                    endPos = Point(rowIdx, colIdx)
                    elevations[endPos] = 'z'
                }
                else -> elevations[Point(rowIdx, colIdx)] = ch
            } }
        }

        return Triple(elevations.toMap(), startPos, endPos)
    }

    override fun part1(input: ParsedInput): Any {
        val (elevations, startPos, endPos) = input
        val result = TraverseGrid.findMinPath(elevations, startPos, endPos)
        return result.size - 1
    }

    override fun part2(input: ParsedInput): Any {
        val (elevations, _, endPos) = input

        val minPath = elevations
            .filterValues { it == 'a' }
            .map { (startPos, _) -> TraverseGrid.findMinPath(elevations, startPos, endPos) }
            .filter { it.isNotEmpty() }
            .minBy { it.size - 1 }

        return minPath.size - 1
    }
}

fun main() {
    Day12.solve(test = false)
}