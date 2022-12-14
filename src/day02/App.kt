package day02

import common.Solution

sealed class Hand() {

    abstract val beats: Hand
    abstract val losesTo: Hand

    fun defeats(hand: Hand): Boolean = hand == this.beats

    object Rock: Hand() {
        override val beats: Hand = Scissors
        override val losesTo: Hand = Paper
    }

    object Paper: Hand() {
        override val beats: Hand = Rock
        override val losesTo: Hand = Scissors
    }

    object Scissors: Hand() {
        override val beats: Hand = Paper
        override val losesTo: Hand = Rock
    }

}

val HAND_POINTS = mapOf(
    Hand.Rock to 1,
    Hand.Paper to 2,
    Hand.Scissors to 3,
)

enum class PlayerStrategy {
    DRAW,
    LOSE,
    WIN,
}

val OPPONENT_ACTION_HAND_GUIDE = mapOf(
    'A' to Hand.Rock,
    'B' to Hand.Paper,
    'C' to Hand.Scissors,
)

fun handForOpponentAction(action: Char): Hand = OPPONENT_ACTION_HAND_GUIDE[action]!!

const val WIN_GAME_POINTS = 6
const val TIE_GAME_POINTS = 3
const val LOSE_GAME_POINTS = 0

object Game {

    fun run (
        actions: List<Pair<Char, Char>>,
        handForYourAction: (action: Char, opponentHand: Hand) -> Hand,
    ): Int {
        return actions.sumOf { (opponentAction, yourAction) ->
            val opponentHand = handForOpponentAction(opponentAction)
            val yourHand = handForYourAction(yourAction, opponentHand)

            HAND_POINTS[yourHand]!! + if (yourHand.defeats(opponentHand)) {
                WIN_GAME_POINTS
            } else if (opponentHand.defeats(yourHand)) {
                LOSE_GAME_POINTS
            } else {
                TIE_GAME_POINTS
            }
        }
    }

}

typealias ParsedInput = List<Pair<Char, Char>>

object Day02 : Solution.LinedInput<ParsedInput>(day = 2) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input.map { line ->
            val values = line.split(" ")
            val charA = values.first().first()
            val charB = values.last().first()
            (charA to charB)
        }
    }

    override fun part1(actions: ParsedInput): Any {
        val actionGuide = mapOf(
            'X' to Hand.Rock,
            'Y' to Hand.Paper,
            'Z' to Hand.Scissors,
        )

        val handForYourAction = { action: Char, _: Hand -> actionGuide[action]!! }

        return Game.run(actions, handForYourAction)
    }

    override fun part2(actions: ParsedInput): Any {
        val actionGuide = mapOf(
            'X' to PlayerStrategy.LOSE,
            'Y' to PlayerStrategy.DRAW,
            'Z' to PlayerStrategy.WIN,
        )

        val handForYourAction = { action: Char, opponentHand: Hand -> when (actionGuide[action]!!) {
            PlayerStrategy.DRAW -> opponentHand
            PlayerStrategy.LOSE -> opponentHand.beats
            PlayerStrategy.WIN -> opponentHand.losesTo
        } }

        return Game.run(actions, handForYourAction)
    }

}

fun main() {
    Day02.solve()
}
