package day08.solution2

// Refactored my original day 8 solution.

import common.Solution

data class Step(val row: Int, val col: Int)

enum class Direction(val step: Step) {
    LEFT(Step(0, -1)),
    UP(Step(-1, 0)),
    RIGHT(Step(0, 1)),
    DOWN(Step(1, 0))
}

data class Point(val row: Int, val col: Int)

typealias Grid = List<List<Int>>

val Grid.width: Int get() = this.first().size
val Grid.height: Int get() = this.size

fun Grid.pointInBounds(row: Int, col: Int): Boolean =
    row >= 0 && row < this.height && col >= 0 && col < this.width
fun Grid.pointInBounds(p: Point): Boolean =
    this.pointInBounds(p.row, p.col)

fun Grid.pointOnEdge(p: Point): Boolean =
    p.row == 0 || p.col == 0 || p.row == this.height - 1 || p.col == this.width - 1

fun Grid.valueAt(p: Point): Int = this[p.row][p.col]
fun Grid.valueAt(row: Int, col: Int): Int = this[row][col]

object Day08 : Solution.LinedInput<Grid>(day = 8) {

    override fun parseInput(input: List<String>): Grid {
        return input.map { row -> row.toList().map { it.digitToInt() } }
    }

    override fun part1(grid: Grid): Any {
        fun pointVisibleInDirection(p: Point, direction: Direction): Boolean {
            if (!grid.pointInBounds(p)) {
                return false
            }

            if (grid.pointOnEdge(p)) {
                return true
            }

            val step = direction.step
            var r = p.row
            var c = p.col

            while (true) {
                r += step.row
                c += step.col

                if (!grid.pointInBounds(r, c)) {
                    break
                }

                if (grid.valueAt(p) <= grid.valueAt(r, c)) {
                    return false
                }
            }

            return true
        }

        fun pointHasVisibility(p: Point) =
            Direction.values().map { dir -> pointVisibleInDirection(p, dir) }.any { it }

        return grid.mapIndexed { rowIdx, row ->
            row.indices.map { colIdx -> pointHasVisibility(Point(rowIdx, colIdx)) }.count { it }
        }.sum()
    }

    override fun part2(grid: Grid): Any {
        fun pointVisibilityScoreInDirection(p: Point, direction: Direction): Int {
            if (!grid.pointInBounds(p)) {
                return -1
            }

            if (grid.pointOnEdge(p)) {
                return 0
            }

            var score = 0
            val step = direction.step
            var r = p.row
            var c = p.col

            while (true) {
                r += step.row
                c += step.col

                if (!grid.pointInBounds(r, c)) {
                    break
                }

                score += 1

                if (grid.valueAt(r, c) >= grid.valueAt(p)) {
                    break
                }
            }

            return score
        }

        fun pointVisibilityScore(p: Point): Int = Direction.values()
            .map { dir -> pointVisibilityScoreInDirection(p, dir) }
            .reduce { acc, value -> acc * value }

        return grid.mapIndexed { rowIdx, row ->
            row.indices.maxOf { colIdx -> pointVisibilityScore(Point(rowIdx, colIdx)) }
        }.max()
    }
}

fun main() {
    Day08.solve(test = true)
}