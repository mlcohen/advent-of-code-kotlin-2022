package day05_second_attempt

import common.readInputGroupedLines

typealias CrateStack = List<Char>
typealias CrateStackList = List<CrateStack>

fun CrateStackList.prettyPrint() {
    val maxStackSize = this.maxOf { it.size }
    val stackRange = (1..maxStackSize)
    val rows =  stackRange.map { row ->
        this.map { stack ->
            if (row > stack.size) ' ' else stack[row - 1]
        }
    }
    rows.reversed().forEach { println(it.joinToString(" ")) }
    println((1..this.size).joinToString(" "))
}

fun main() {
    val (crateStacksLayout, moveActions) = readInputGroupedLines("input/day05_test")

    val numStacks = """\d+""".toRegex().findAll(crateStacksLayout.last()).map { it.groupValues[0] }.toList().size
    val stacks = mutableListOf<CrateStack>()

    for (i_ in 1..numStacks) stacks.add(listOf())

    crateStacksLayout.dropLast(1).forEach { line ->
        line
            .chunked(4)
            .map { """\w""".toRegex().find(it)?.groupValues?.get(0) ?: "" }
            .forEachIndexed { idx, crate ->
                if (crate.isNotEmpty()) {
                    stacks[idx] = stacks[idx] + crate[0]
                }
            }
    }
    stacks.forEachIndexed { idx, stack -> stacks[idx] = stack.reversed() }

    for (line in moveActions) {
        val (amount, from_, to_) = """\d+""".toRegex()
            .findAll(line)
            .map { it.groupValues[0] }
            .toList()
            .map(String::toInt)
        val fromStack = stacks[from_ - 1]
        val toStack = stacks[to_ - 1]
        val pickedCrates = fromStack.takeLast(amount).reversed()
        stacks[to_ - 1] = toStack + pickedCrates
        stacks[from_ - 1] = fromStack.dropLast(amount)
    }

    val message = stacks.map { it.last() }.joinToString("")

    println("message = $message\n")

    stacks.prettyPrint()
}