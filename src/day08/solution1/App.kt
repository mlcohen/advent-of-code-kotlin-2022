package day08.solution1

// Original solution used to solve the AoC day 8 puzzle. Warts and all.

import common.Solution

enum class Direction { LEFT, UP, RIGHT, DOWN }

data class Point(val row: Int, val col: Int)

typealias IntGrid = List<List<Int>>

val IntGrid.width: Int get() = this.first().size
val IntGrid.height: Int get() = this.size

fun IntGrid.pointInBounds(p: Point): Boolean =
    p.row >= 0 && p.row < this.height && p.col >= 0 && p.col < this.width

fun IntGrid.pointOnEdge(p: Point): Boolean =
    p.row == 0 || p.col == 0 || p.row == this.height - 1 || p.col == this.width - 1

fun IntGrid.valueAt(p: Point): Int = this[p.row][p.col]
fun IntGrid.valueAt(row: Int, col: Int): Int = this[row][col]

object Day08 : Solution.LinedInput<IntGrid>(day = 8) {

    override fun parseInput(input: List<String>): IntGrid {
        return input.mapIndexed() { rowIdx, line ->
            line.toList().mapIndexed { colIdx, value -> value.digitToInt() }
        }
    }

    override fun part1(grid: IntGrid): Any {
        fun pointVisibleInDirection(p: Point, direction: Direction): Boolean {
            if (!grid.pointInBounds(p)) {
                return false
            }

            if (grid.pointOnEdge(p)) {
                return true
            }

            return when (direction) {
                Direction.LEFT -> (0 until p.col).map { c -> grid.valueAt(p) > grid.valueAt(p.row, c)
                }.all { it }
                Direction.UP -> (0 until p.row).map { r ->
                    grid.valueAt(p) > grid.valueAt(r, p.col)
                }.all { it }
                Direction.RIGHT -> (p.col + 1 until grid.width).map { c ->
                    grid.valueAt(p) > grid.valueAt(p.row, c)
                }.all { it }
                Direction.DOWN -> (p.row + 1 until grid.height).map { r ->
                    grid.valueAt(p) > grid.valueAt(r, p.col)
                }.all { it }
            }
        }

        fun pointHasVisibility(p: Point) = Direction.values().map { dir -> pointVisibleInDirection(p, dir) }.any { it }

//        grid.forEachIndexed { rowIdx, row ->
//            val s = row.mapIndexed { colIdx, value ->
//                val visible = pointHasVisibility(Point(rowIdx, colIdx))
//                "[$value:${visible.toString()[0]}]"
//            }.joinToString(" ")
//            println(s)
//        }

        return grid.mapIndexed { rowIdx, row ->
            row.indices.map { colIdx -> pointHasVisibility(Point(rowIdx, colIdx)) }.count { it }
        }.sum()
    }

    override fun part2(grid: IntGrid): Any {
        fun pointVisibilityScoreInDirection(p: Point, direction: Direction): Int {
            if (!grid.pointInBounds(p)) {
                return -1
            }

            if (grid.pointOnEdge(p)) {
                return 0
            }

            return when (direction) {
                Direction.LEFT -> {
                    var score = 0
                    var c = p.col - 1
                    while (c >= 0) {
                        score += 1
                        if (grid.valueAt(p.row, c) >= grid.valueAt(p)) {
                            break
                        }
                        c -= 1
                    }
                    score
                }
                Direction.UP -> {
                    var score = 0
                    var r = p.row - 1
                    while (r >= 0) {
                        score += 1
                        if (grid.valueAt(r, p.col) >= grid.valueAt(p)) {
                            break
                        }
                        r -= 1
                    }
                    score
                }
                Direction.RIGHT -> {
                    var score = 0
                    var c = p.col + 1
                    while (c < grid.width) {
                        score += 1
                        if (grid.valueAt(p.row, c) >= grid.valueAt(p)) {
                            break
                        }
                        c += 1
                    }
                    score
                }
                Direction.DOWN -> {
                    var score = 0
                    var r = p.row + 1
                    while (r < grid.height) {
                        score += 1
                        if (grid.valueAt(r, p.col) >= grid.valueAt(p)) {
                            break
                        }
                        r += 1
                    }
                    score
                }
            }
        }

        fun pointVisibilityScore(p: Point): Int = Direction.values().map { dir ->
            pointVisibilityScoreInDirection(p, dir) }.reduce { acc, value -> acc * value }

//        grid.forEachIndexed { rowIdx, row ->
//            val s = row.mapIndexed { colIdx, value ->
//                val score = pointVisibilityScore(Point(rowIdx, colIdx))
//                "[$value:${score}]"
//            }.joinToString(" ")
//            println(s)
//        }

        return grid.mapIndexed { rowIdx, row ->
            row.indices.map { colIdx -> pointVisibilityScore(Point(rowIdx, colIdx)) }.max()
        }.max()

        return Unit
    }
}

fun main() {
    Day08.solve(test = false)
}