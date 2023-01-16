package day18.solution1

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
        return cubes.sumOf { cube -> Directions.count { dir -> (cube + dir) !in cubes } }
    }

    override fun part2(input: ParsedInput): Any {
        val cubes = input.toSet()
        val maxX = cubes.maxOf { it.x }
        val maxY = cubes.maxOf { it.y }
        val maxZ = cubes.maxOf { it.z }
        var exposedFaces = cubes.sumOf { cube -> Directions.count { dir -> (cube + dir) !in cubes } }

        val airCubes = cubes
            .map { cube -> Directions.map { dir -> dir + cube }.filter { it !in cubes } }
            .flatten()
            .toSet()

        val airPockets = mutableListOf<Set<Vec3>>()
        val candidateAirCubes = airCubes.toMutableSet()

        while (candidateAirCubes.isNotEmpty()) {
            val candidateAirCube = candidateAirCubes.first()
            candidateAirCubes.remove(candidateAirCube)
            var queue = listOf(candidateAirCube)
            val airPocket = mutableSetOf<Vec3>()

            while (queue.isNotEmpty()) {
                val airCube = queue.first()

                for (d in Directions) {
                    val cube = d + airCube
                    if (cube in airPocket || cube in cubes || cube in queue) {
                        continue
                    }
                    candidateAirCubes.remove(cube)
                    queue += cube
                }

                if (airCube.x !in 0..maxX || airCube.y !in 0..maxY || airCube.z !in 0..maxZ) {
                    break
                }

                airPocket.add(airCube)
                queue = queue.drop(1)
            }

            if (queue.isEmpty()) {
                airPockets.add(airPocket.toSet())
            }
        }

        val enclosedFaces = airPockets.sumOf { airPocket ->
            airPocket.sumOf { airCube ->
                Directions.map { it + airCube }.count { it in cubes }
            }
        }

        return exposedFaces - enclosedFaces
    }
}

fun main() {
    Day18.solve(test = false)
}