package day05.solution3

// Took original solution (solution 1) and did some refactoring

import common.Solution

data class MoveAction(val amount: Int, val from: Int, val to: Int);
typealias CrateStack = List<Char>
typealias CrateStacks = List<CrateStack>
typealias MoveActions = List<MoveAction>

interface CratePicker {
    fun pickCratesFromStack(stack: List<Char>, numCrates: Int): List<Char>;
}

class CrateStackOperator {
    private val cratePicker: CratePicker;

    constructor(cratePicker: CratePicker) {
        this.cratePicker = cratePicker
    }

    fun applyMove(crateStacks: CrateStacks, moveAction: MoveAction): CrateStacks {
        val fromStack = crateStacks[moveAction.from]
        val toStack = crateStacks[moveAction.to]
        val crates = this.cratePicker.pickCratesFromStack(fromStack, moveAction.amount)

        return crateStacks.mapIndexed { idx, stack ->
            when (idx) {
                moveAction.to -> toStack + crates
                moveAction.from -> fromStack.dropLast(moveAction.amount)
                else -> stack
            }
        }
    }

    fun applyMoves(crateStacks: CrateStacks, moveActions: MoveActions): CrateStacks {
        return moveActions.fold(crateStacks) { currCrateStacks, action ->
            this.applyMove(currCrateStacks, action)
        }
    }
}

object CratePicker9000: CratePicker {
    override fun pickCratesFromStack(stack: List<Char>, numCrates: Int): List<Char> {
        return stack.takeLast(numCrates).reversed()
    }
}

object CratePicker9001: CratePicker {
    override fun pickCratesFromStack(stack: List<Char>, numCrates: Int): List<Char> {
        return stack.takeLast(numCrates)
    }
}

fun CrateStacks.print(): Unit {
    this.forEachIndexed { idx, stack ->
        println("Stack ${idx + 1}: $stack")
    }
}

fun CrateStacks.message(): String {
    return this.map { if (it.isNotEmpty()) it.last() else "" }.joinToString("")
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
                val amount = tokens[1].toInt()
                val from_ = tokens[3].toInt() - 1
                val to_ = tokens[5].toInt() - 1
                MoveAction(amount, from_, to_)
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
        val operator = CrateStackOperator(cratePicker = CratePicker9000)
        return operator.applyMoves(crateStacks, moveActions).message()
    }

    override fun part2(input: ParsedInput): Any {
        val (crateStacks, moveActions) = input
        val operator = CrateStackOperator(cratePicker = CratePicker9001)
        return operator.applyMoves(crateStacks, moveActions).message()
    }
}

fun main() {
    Day05.solve(test = false)
}