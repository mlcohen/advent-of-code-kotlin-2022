package day17.solution1

// Original solution used to solve the AoC day 17 puzzle

import common.Solution
import kotlin.math.max
import kotlin.math.min

data class Vec2(val x: Int, val y: Int) {
    operator fun plus(v: Vec2): Vec2 {
        return Vec2(this.x + v.x, this.y + v.y)
    }
}

typealias Point = Vec2
typealias Offset = Vec2

enum class Direction(val offset: Offset) {
    UP(Offset(0, 1)),
    DOWN(Offset(0, -1)),
    LEFT(Offset(-1, 0)),
    RIGHT(Offset(1, 0));

    operator fun plus(d: Direction): Offset {
        return this.offset + d.offset
    }
}

val ROCK_SHAPE1 = "####"
val ROCK_SHAPE2 = """
    .#.
    ###
    .#.
""".trimIndent()
val ROCK_SHAPE3 = """
    ..#
    ..#
    ###
""".trimIndent()
val ROCK_SHAPE4 = """
    #
    #
    #
    #
""".trimIndent()
val ROCK_SHAPE5 = """
    ##
    ##
""".trimIndent()

val ROCK_SHAPES = listOf(ROCK_SHAPE1, ROCK_SHAPE2, ROCK_SHAPE3, ROCK_SHAPE4, ROCK_SHAPE5)

data class Rock(val shape: String) {
    val grid = shape.split('\n').map { line -> line.toList() }
    val width = grid.first().size
    val height = grid.size

    fun contains(row: Int, col: Int): Boolean {
        return (row in 0 until height) && (col in 0 until width)
    }

    fun elementAt(row: Int, col: Int): Boolean {
        if (!this.contains(row, col)) {
            return false
        }

        return this.grid[row][col] == '#'
    }
}

data class PositionedRock(val rock: Rock, val position: Point) {
    val left = position.x
    val right = position.x + rock.width - 1
    val top = position.y
    val bottom = position.y - rock.height + 1

    fun move(d: Direction): PositionedRock {
        return this.copy(position = position + d.offset)
    }

    operator fun contains(p: Point): Boolean {
        return (p.x in this.left..this.right) && (p.y in this.bottom..this.top)
    }

    fun hasElementAt(x: Int, y: Int): Boolean {
        return this.hasElementAt(Point(x, y))
    }

    fun hasElementAt(p: Point): Boolean {
        if (p !in this) {
            return false
        }

        val row = this.top - p.y
        val col = p.x - this.left

        return this.rock.elementAt(row, col)
    }
}

class Chamber(val width: Int) {

    private var grid: MutableList<MutableList<Char>> = mutableListOf()
    val height: Int get() = grid.size

    fun contains(x: Int, y: Int): Boolean {
        return (x in  0 until this.width) && (y in 0 until this.height)
    }

    fun hasSettledRockAt(x: Int, y: Int): Boolean {
        if (!this.contains(x, y)) {
            return false
        }

        return grid[y][x] == '#'
    }

    fun rockTouchesWall(rock: PositionedRock): Boolean {
        return rock.left < 0 || rock.right >= this.width
    }

    fun rockItersectsSettledRock(rock: PositionedRock): Boolean {
        for (y in rock.bottom..rock.top) {
            for (x in 0 until this.width) {
                if (this.hasSettledRockAt(x, y) && rock.hasElementAt(x, y)) {
                    return true
                }
            }
        }
        return false
    }

    fun rockTouchesFloor(rock: PositionedRock): Boolean {
        return rock.bottom < 0
    }

    fun settleRock(rock: Rock, position: Point) {
        this.settleRock(PositionedRock(rock, position))
    }

    fun settleRock(rock: PositionedRock) {
        if (rock.top + 1 > this.height) {
            for (i in 0 .. (rock.top - this.height)) {
                this.grid.add(MutableList(this.width) { '.' })
            }
        }

        for (y in rock.bottom..rock.top) {
            for (x in rock.left..rock.right) {
                if (rock.hasElementAt(x, y)) {
                    this.grid[y][x] = '#'
                }
            }
        }
    }

}

enum class DropRockAction {
    JET_PUSH_ROCK_LEFT,
    JET_PUSH_ROCK_RIGHT,
}

enum class DropRockState {
    FALLING,
    AT_REST,
}

class DropRockIterator(
    private val rock: Rock,
    private val chamber: Chamber,
) {
    private var _state: DropRockState = DropRockState.FALLING
    private var _fallingRock: PositionedRock

    val state: DropRockState get() = this._state
    val fallingRock: PositionedRock get() = this._fallingRock

    init {
        val position = Point(x = 2, y = (chamber.height - 1) + rock.height + 3)
        this._fallingRock = PositionedRock(rock, position)
    }

    fun hasNext(): Boolean {
        return this._state == DropRockState.FALLING
    }

    fun next(action: DropRockAction) {
        val moveDirection = when (action) {
            DropRockAction.JET_PUSH_ROCK_LEFT -> Direction.LEFT
            DropRockAction.JET_PUSH_ROCK_RIGHT -> Direction.RIGHT
        }

        for (direction in listOf(moveDirection, Direction.DOWN)) {
            val (movedRock, rockStopped) = when (direction) {
                Direction.LEFT, Direction.RIGHT -> {
                    val movedRock = this._fallingRock.move(direction)
                    if (this.chamber.rockTouchesWall(movedRock)) {
                        (this._fallingRock to false)
                    } else if (this.chamber.rockItersectsSettledRock(movedRock)) {
                        (this._fallingRock to false)
                    } else {
                        (movedRock to false)
                    }
                }
                Direction.DOWN -> {
                    val movedRock = this._fallingRock.move(direction)
                    if (this.chamber.rockItersectsSettledRock(movedRock)) {
                        (this._fallingRock to true)
                    } else if (this.chamber.rockTouchesFloor(movedRock)) {
                        (this._fallingRock to true)
                    } else {
                        (movedRock to false)
                    }
                }
                else -> throw error("Unexpected direction $direction")
            }

            this._fallingRock = movedRock

            if (rockStopped) {
                chamber.settleRock(movedRock)
                this._state = DropRockState.AT_REST
                break
            }
        }
    }
}

class DropRocksIterator(
    val chamber: Chamber,
    val jetPattern: String,
    val rockSequence: List<Rock>,
) {
    var rockSequenceCounter: Int = 1
    var jetPatternCounter: Int = 0
    var currentDropRockIterator: DropRockIterator = DropRockIterator(rockSequence.first(), chamber)

    val fallingRock: PositionedRock get() = this.currentDropRockIterator.fallingRock

    private fun prepareNextRockToDrop() {
        val rockSequenceIndex = rockSequenceCounter % rockSequence.size
        val nextRock = rockSequence[rockSequenceIndex]
        this.currentDropRockIterator = DropRockIterator(nextRock, chamber)
        rockSequenceCounter += 1
    }

    private fun takeNextDropRockStep(): Boolean {
        if (!currentDropRockIterator.hasNext()) {
            return true
        }

        val jetPatterIndex = this.jetPatternCounter % this.jetPattern.length
        val action = when (this.jetPattern[jetPatterIndex]) {
            '>' -> DropRockAction.JET_PUSH_ROCK_RIGHT
            '<' -> DropRockAction.JET_PUSH_ROCK_LEFT
            else -> throw error("Invalid jet action at index ${jetPatterIndex}")
        }
        currentDropRockIterator.next(action)
        this.jetPatternCounter += 1

        return false
    }

    fun dropNextRock() {
        while (true) {
            val done = this.takeNextDropRockStep()
            if (done) {
                break
            }
        }

        this.prepareNextRockToDrop()
    }

    fun nextDropRockStep() {
        val done = this.takeNextDropRockStep()

        if (done) {
            this.prepareNextRockToDrop()
            return
        }
    }
}

class DropRocksRunner {

    fun dropRocks(numRocks: Int, jetPattern: String, rockSequence: List<String>, chamberWidth: Int = 7): Chamber {
        val chamber = Chamber(width = chamberWidth)
        val iter = DropRocksIterator(
            chamber = chamber,
            jetPattern = jetPattern,
            rockSequence = rockSequence.map { Rock(it) }
        )
        val printer = PrettyPrinter()

        (1..numRocks).forEach { step ->
//            println("*** Drop Rock $step")
            iter.dropNextRock()
//            printer.print(chamber)
//            println()
        }

        return chamber
    }

}

class Canvas(val width: Int, val height: Int, defaultCellChar: Char = ' ') {
    private val grid: MutableList<MutableList<Char>> = MutableList(height) { MutableList(width) { defaultCellChar } }

    fun drawCell(c: Char, row: Int, col: Int): Canvas {
        if (row !in 0 until this.height || col !in 0 until this.width) {
            return this
        }

        this.grid[row][col] = c

        return this
    }

    fun drawRowLine(c: Char, row: Int, fromCol: Int, toCol: Int): Canvas {
        val minCol = min(fromCol, toCol)
        val maxCol = max(fromCol, toCol)
        for (col in minCol..maxCol) {
            this.drawCell(c, row, col)
        }
        return this
    }

    fun drawColumnLine(c: Char, col: Int, fromRow: Int, toRow: Int): Canvas {
        val minRow = min(fromRow, toRow)
        val maxRow = max(fromRow, toRow)
        for (row in minRow..maxRow) {
            this.drawCell(c, row, col)
        }
        return this
    }

    fun forEachRow(fn: (List<Char>) -> Unit) {
        this.grid.forEach { row -> fn(row.toList()) }
    }

    fun print() {
        this.forEachRow { row ->
            println(row.joinToString(" "))
        }
    }
}

class PrettyPrinter() {
    fun print(chamber: Chamber, fallingRock: PositionedRock? = null) {
        val canvas = Canvas(
            width = chamber.width + 2,
            height = (fallingRock?.let { it.top + 1 } ?: chamber.height) + 1,
            defaultCellChar = '.',
        )

        this.drawChamberFloor(canvas, chamber)
        this.drawChamberWalls(canvas, chamber)
        this.drawChamberRocks(canvas, chamber)

        fallingRock?.let { this.drawFallingRock(canvas, it) }

        canvas.print()
    }

    private fun drawChamberFloor(canvas: Canvas, chamber: Chamber) {
        val floorRow = canvas.height - 1
        canvas
            .drawCell('+', floorRow, 0)
            .drawCell('+', floorRow, canvas.width - 1)
            .drawRowLine('\u2014', row = floorRow, fromCol = 1, toCol = canvas.width - 2)
    }

    private fun drawChamberWalls(canvas: Canvas, chamber: Chamber) {
        canvas
            .drawColumnLine('|', col = 0, fromRow = 0, toRow = canvas.height - 2)
            .drawColumnLine('|', col = canvas.width - 1, fromRow = 0, toRow = canvas.height - 2)
    }

    private fun drawChamberRocks(canvas: Canvas, chamber: Chamber) {
        for (y in 0 until chamber.height) {
            for (x in 0 until chamber.width) {
                if (chamber.hasSettledRockAt(x, y)) {
                    val col = x + 1
                    val row = canvas.height - y - 2
                    canvas.drawCell('#', row, col)
                }
            }
        }
    }

    private fun drawFallingRock(canvas: Canvas, rock: PositionedRock) {
        for (x in rock.left..rock.right) {
            for (y in rock.bottom..rock.top) {
                if (rock.hasElementAt(x, y)) {
                    val col = x + 1
                    val row = canvas.height - y - 2
                    canvas.drawCell('@', row, col)
                }
            }
        }
    }
}

typealias ParsedInput = String

object Day17 : Solution.LinedInput<ParsedInput>(day = 17) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input.map{ line ->
            line
        }.first()
    }

    override fun part1(input: ParsedInput): Any {
        val rockSequence = ROCK_SHAPES
        val runner = DropRocksRunner()
        val chamber = runner.dropRocks(2022, input, rockSequence)
        return chamber.height
    }

    override fun part2(input: ParsedInput): Any {
        return Unit
    }
}

fun main() {
    Day17.solve(test = false)
}