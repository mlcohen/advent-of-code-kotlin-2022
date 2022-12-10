package day09.solution1

// Original solution used to solve the AoC day 9 puzzle.

import common.Solution
import kotlin.math.abs

data class Offset(val x: Int, val y: Int)

enum class Direction(val offset: Offset) {
    UP(Offset(0, 1)),
    LEFT(Offset(-1, 0)),
    DOWN(Offset(0, -1)),
    RIGHT(Offset(1, 0)),
}

data class Action(val direction: Direction, val steps: Int)

data class Position(val x: Int, val y: Int) {
    fun moveBy(offsetX: Int, offsetY: Int): Position {
        return Position(this.x + offsetX, this.y + offsetY)
    }

    fun moveNear(p: Position): Position {
        val dx = p.x - this.x
        val dy = p.y - this.y
        val adx = abs(dx)
        val ady = abs(dy)

        if (adx <= 1 && ady <= 1) {
            return this
        }

        val offsetX = when {
            adx == 0 -> 0
            adx < ady -> 0
            else -> if (dx > 0) -1 else 1
        }

        val offsetY = when {
            ady == 0 -> 0
            ady < adx -> 0
            else -> if (dy > 0) -1 else 1
        }

        return Position(p.x + offsetX, p.y + offsetY)
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}

fun moveKnots(actions: List<Action>, knotCount: Int = 2): Pair<List<Position>, Set<Position>> {
    var knots = IntRange(1, knotCount).toList().map { Position(0, 0) }
    var visitedTailPositions = mutableSetOf(Position(0, 0))

    for (action in actions) {
        val (direction, steps) = action
        for (step in 1..steps) {
            val head = knots.first()
            val nextHead = head.moveBy(direction.offset.x, direction.offset.y)
            var nextKnots = listOf(nextHead)
            for (i in 1 until knots.size) {
                val leader = nextKnots[i - 1]
                var nextKnot = knots[i]
                nextKnots += nextKnot.moveNear(leader)
            }
            knots = nextKnots
            visitedTailPositions.add(knots.last())
        }
    }

    return (knots to visitedTailPositions.toSet())
}

typealias ParsedInput = List<Action>

object Day09 : Solution.LinedInput<ParsedInput>(day = 9) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input.map{ line -> line.split(" ").let { (a, b) ->
            val direction = when (a) {
                "U" -> Direction.UP
                "D" -> Direction.DOWN
                "L" -> Direction.LEFT
                "R" -> Direction.RIGHT
                else -> throw error("Invalid direction $a")
            }
            Action(direction, steps = b.toInt())
        } }
    }

    override fun part1(actions: ParsedInput): Any {
        val (_, visited) = moveKnots(actions, knotCount = 2)
        return visited.size
    }

    override fun part2(actions: ParsedInput): Any {
        val (_, visited) = moveKnots(actions, knotCount = 10)
        return visited.size
    }
}

fun main() {
    Day09.solve(test = false)
}