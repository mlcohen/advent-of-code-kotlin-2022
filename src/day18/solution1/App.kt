package day18

// Original solution used to solve the AoC day 18 puzzle.

import common.Solution

data class Vec3(val x: Int, val y: Int, val z: Int) {
    operator fun plus(v: Vec3): Vec3 = Vec3(this.x + v.x, this.y + v.y, this.z + v.z)
    override fun toString(): String = "($x, $y, $z)"
}

val Directions: List<Vec3> = listOf(
    Vec3(1, 0, 0),
    Vec3(0, 1, 0),
    Vec3(0, 0, 1),
    Vec3(-1, 0, 0),
    Vec3(0, -1, 0),
    Vec3(0, 0, -1),
)

typealias Point = Vec3

typealias ParsedInput = List<Vec3>

object Day18 : Solution.LinedInput<ParsedInput>(day = 18) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input.map{ line ->
            line.split(',').map(String::toInt).let { (x, y, z) -> Vec3(x, y, z) }
        }
    }

    override fun part1(input: ParsedInput): Any {
        val cubes = input.toSet()
        val exposedFaces = cubes.fold(0) { count, cube ->
            count + Directions.count { dir -> (cube + dir) !in cubes }
        }
        return exposedFaces
    }

    override fun part2(input: ParsedInput): Any {
        return Unit
    }
}

fun main() {
    Day18.solve(test = false)
}