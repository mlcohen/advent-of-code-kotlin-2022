package day13.solution1

// Original solution used to solve the AoC day 13 puzzle

import common.Solution
import kotlin.math.min

sealed class PacketData {
    data class Int_(val value: Int): PacketData() {
        override fun toString(): String = value.toString()
    }
    data class List_(val values: List<PacketData> = listOf()): PacketData() {
        val size: Int = values.size
        operator fun plus(value: PacketData): List_ = List_(this.values + value)
        operator fun get(idx: Int): PacketData = this.values[idx]
        fun first(): PacketData = this.values.first()
        override fun toString(): String = values.toString()
    }
}

object PacketDataStringParser {

    fun parse(line: String): PacketData {
        var stack = listOf<PacketData.List_>()
        var intToken = ""

        fun completeList() {
            if (stack.size > 1) {
                val (peek, top) = stack.takeLast(2)
                stack = stack.dropLast(2) + (peek + top)
            }
        }

        fun completeIntToken() {
            if (intToken.isNotEmpty()) {
                val value = intToken.toInt()
                val top = stack.last()
                stack = stack.dropLast(1) + (top + PacketData.Int_(value))
                intToken = ""
            }
        }

        for (c in line.toList()) {
            when (c) {
                '[' -> stack += PacketData.List_()
                ']' -> {
                    completeIntToken()
                    completeList()
                }
                in '0'..'9' -> intToken = "$intToken$c"
                ',' -> completeIntToken()
                else -> throw error("Unexpected char $c")
            }
        }

        if (stack.size != 1) {
            throw error("Unable to parse $line")
        }

        return stack.last()
    }

}

enum class PacketPairOrderCompareResult {
    CORRECT_ORDER,
    INCORRECT_ORDER,
    SAME,
}

class PacketComparitor {

    fun compare(left: PacketData, right: PacketData, depth: Int = 0): PacketPairOrderCompareResult {
        return when {
            left is PacketData.Int_ && right is PacketData.Int_ -> {
                this.compareInts(left, right, depth + 1)
            }
            left is PacketData.Int_ && right is PacketData.List_ -> {
                this.compareLists(PacketData.List_(listOf(left)), right, depth + 1)
            }
            left is PacketData.List_ && right is PacketData.Int_ -> {
                this.compareLists(left, PacketData.List_(listOf(right)), depth + 1)
            }
            left is PacketData.List_ && right is PacketData.List_ -> {
                this.compareLists(left, right, depth + 1)
            }
            else -> throw error("Invalid comparison: left = $left, right = $right")
        }
    }

    private fun compareInts(left: PacketData.Int_, right: PacketData.Int_, depth: Int = 0): PacketPairOrderCompareResult {
        if (left.value < right.value) {
            return PacketPairOrderCompareResult.CORRECT_ORDER
        }

        if (left.value > right.value) {
            return PacketPairOrderCompareResult.INCORRECT_ORDER
        }

        return PacketPairOrderCompareResult.SAME
    }

    private fun compareLists(left: PacketData.List_, right: PacketData.List_, depth: Int = 0): PacketPairOrderCompareResult {
        val minSize = min(left.size, right.size)

        for (i in 0 until minSize) {
            val outcome = this.compare(left[i], right[i], depth)
            if (outcome != PacketPairOrderCompareResult.SAME) {
                return outcome
            }
        }

        if (left.size < right.size) {
            return PacketPairOrderCompareResult.CORRECT_ORDER
        }

        if (right.size < left.size) {
            return PacketPairOrderCompareResult.INCORRECT_ORDER
        }

        return PacketPairOrderCompareResult.SAME
    }

}

typealias ParsedInput = List<Pair<PacketData, PacketData>>

object Day13 : Solution.GroupedLinedInput<ParsedInput>(day = 13) {

    override fun parseInput(input: List<List<String>>): ParsedInput {
        return input.map{ group ->
            group.map { PacketDataStringParser.parse(it) }.let { (a, b) -> (a to b) }
        }
    }

    override fun part1(input: ParsedInput): Any {
        val comparitor = PacketComparitor()

        val result = input.map {
            val (left, right) = it
            val result = comparitor.compare(left, right)
            (it to result)
        }

//        result.forEach { (pair, result) ->
//            println(pair.first)
//            println(pair.second)
//            println("result = $result")
//            println()
//        }

        val sum = result.foldIndexed(0) { idx, sum, value ->
            sum + if (value.second == PacketPairOrderCompareResult.CORRECT_ORDER) {
                idx + 1
            } else {
                0
            }
        }

        return sum
    }

    override fun part2(input: ParsedInput): Any {
        val divider1 = PacketDataStringParser.parse("[[2]]")
        val divider2 = PacketDataStringParser.parse("[[6]]")
        val dividerPair = (divider1 to divider2)
        val packets = (input + dividerPair).map { (a, b) -> listOf(a, b) }.flatten()
        val comparitor = PacketComparitor()

        val orderedPackets = packets.sortedWith { left: PacketData, right: PacketData ->
            when (val outcome = comparitor.compare(left, right)) {
                PacketPairOrderCompareResult.CORRECT_ORDER -> -1
                PacketPairOrderCompareResult.INCORRECT_ORDER -> 1
                else -> 0
            }
        }

        val divider1Idx = orderedPackets.indexOf(divider1) + 1
        val divider2Idx = orderedPackets.indexOf(divider2) + 1

        return divider1Idx * divider2Idx
    }
}

fun main() {
    Day13.solve(test = false)
}