package common

import kotlin.math.abs

data class Offset(val row: Int, val col: Int)

enum class Direction(val offset: Offset) {
    UP(Offset(-1, 0)),
    DOWN(Offset(1, 0)),
    LEFT(Offset(0, -1)),
    RIGHT(Offset(0, 1)),
}

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