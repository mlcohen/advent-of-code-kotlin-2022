package day05.solution1

// Original solution used to solve the AoC day 5 puzzle. Warts and all.

import common.Solution

data class MoveAction(val numCrates: Int, val from: Int, val to: Int);
typealias CrateStack = List<Char>
typealias CrateStacks = List<CrateStack>
typealias MoveActions = List<MoveAction>

abstract class CrateStackOperator {
    fun apply(crateStacks: CrateStacks, moveAction: MoveAction): CrateStacks {
        val fromStack = crateStacks[moveAction.from]
        val toStack = crateStacks[moveAction.to]
        val crates = this.pickCratesFromStack(fromStack, moveAction.numCrates)

        return crateStacks.mapIndexed { idx, stack ->
            when (idx) {
                moveAction.to -> toStack + crates
                moveAction.from -> fromStack.dropLast(moveAction.numCrates)
                else -> stack
            }
        }
    }

    protected abstract fun pickCratesFromStack(stack: List<Char>, numCrates: Int): List<Char>;
}

object CrateStackOperator9000: CrateStackOperator() {
    override fun pickCratesFromStack(stack: List<Char>, numCrates: Int): List<Char> {
        return stack.takeLast(numCrates).reversed()
    }
}

object CrateStackOperator9001: CrateStackOperator() {
    override fun pickCratesFromStack(stack: List<Char>, numCrates: Int): List<Char> {
        return stack.takeLast(numCrates)
    }
}

fun CrateStacks.print(): Unit {
    this.forEachIndexed { idx, stack ->
        println("Stack ${idx + 1}: $stack")
    }
}

typealias ParsedInput = Pair<CrateStacks, MoveActions>

object Day05 : Solution.GroupedLinedInput<ParsedInput>(day = 5) {

    private fun parseCrateInputSegment(input: List<String>): CrateStacks {
        val chunkedLines = input.map { line ->
            line.chunked(4).map { it.trim() }
        }
        val crateStacks = (1..chunkedLines.last().last().toInt()).map { mutableListOf<Char>() }

        chunkedLines.dropLast(1).reversed().forEach { crates ->
            crates.forEachIndexed { idx, crate ->
                if (crate.isNotEmpty()) {
                    crateStacks[idx].add(crate[1])
                }
            }
        }

        return crateStacks
    }

    private fun parseMoveActionsInputSegment(input: List<String>): MoveActions {
        return input.map { line ->
            line.split(" ").let { tokens ->
                val numCrates = tokens[1]!!.toInt()
                val fromIdx = tokens[3]!!.toInt() - 1
                val toIdx = tokens[5]!!.toInt() - 1
                MoveAction(numCrates = numCrates, from = fromIdx, to = toIdx)
            }
        }
    }

    override fun parseInput(input: List<List<String>>): ParsedInput {
        val crateStacks = this.parseCrateInputSegment(input.first())
        val moveActions = this.parseMoveActionsInputSegment(input.last())

        return (crateStacks to moveActions);
    }

    override fun part1(input: ParsedInput): Any {
        val (crateStacks, moveActions) = input

        return moveActions.fold(crateStacks) { currCrateStacks, action ->
            CrateStackOperator9000.apply(currCrateStacks, action)
        }.map { it.last() }.joinToString(separator = "") { it.toString() }
    }

    override fun part2(input: ParsedInput): Any {
        val (crateStacks, moveActions) = input

        return moveActions.fold(crateStacks) { currCrateStacks, action ->
            CrateStackOperator9001.apply(currCrateStacks, action)
        }.map { it.last() }.joinToString(separator = "") { it.toString() }
    }
}

fun main() {
    Day05.solve(test = false)
}