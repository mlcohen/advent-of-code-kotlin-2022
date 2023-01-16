package day17.solution1

// Original solution used to solve the AoC day 17 puzzle

import common.Solution
import kotlin.math.max
import kotlin.math.min

data class Vec2(val x: Long, val y: Long) {
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

    constructor(shape: String, position: Point) : this(Rock(shape), position)

    fun move(d: Direction): PositionedRock {
        return this.copy(position = position + d.offset)
    }

    operator fun contains(p: Point): Boolean {
        return (p.x in this.left..this.right) && (p.y in this.bottom..this.top)
    }

    fun hasElementAt(x: Long, y: Long): Boolean {
        return this.hasElementAt(Point(x, y))
    }

    fun hasElementAt(p: Point): Boolean {
        if (p !in this) {
            return false
        }

        val row = this.top - p.y
        val col = p.x - this.left

        return this.rock.elementAt(row.toInt(), col.toInt())
    }
}

interface Chamber {
    val height: Long
    val width: Long
    fun rowAt(y: Long): List<Char>
    fun takeTopRows(num: Int): List<List<Char>>
    fun contains(x: Long, y: Long): Boolean
    fun hasSettledRockAt(x: Long, y: Long): Boolean
    fun rockTouchesWall(rock: PositionedRock): Boolean
    fun rockItersectsSettledRock(rock: PositionedRock): Boolean
    fun rockTouchesFloor(rock: PositionedRock): Boolean
    fun settleRock(rock: Rock, position: Point)
    fun settleRock(rock: PositionedRock)
    fun prettyPrint(limit: Int?)
}

class SimpleChamber(override val width: Long) : Chamber {

    private var grid: MutableList<MutableList<Char>> = mutableListOf()
    override val height: Long get() = grid.size.toLong()

    override fun rowAt(y: Long): List<Char> {
        return this.grid[y.toInt()].toList()
    }

    override fun takeTopRows(num: Int): List<List<Char>> {
        return this.grid.let { if (num > this.height) it.toList() else it.takeLast(num) }.map { it.toList() }
    }

    override fun contains(x: Long, y: Long): Boolean {
        return (x in  0 until this.width) && (y in 0 until this.height)
    }

    override fun hasSettledRockAt(x: Long, y: Long): Boolean {
        if (!this.contains(x, y)) {
            return false
        }

        return grid[y.toInt()][x.toInt()] == '#'
    }

    override fun rockTouchesWall(rock: PositionedRock): Boolean {
        return rock.left < 0 || rock.right >= this.width
    }

    override fun rockItersectsSettledRock(rock: PositionedRock): Boolean {
        for (y in rock.bottom..rock.top) {
            for (x in 0 until this.width) {
                if (this.hasSettledRockAt(x, y) && rock.hasElementAt(x, y)) {
                    return true
                }
            }
        }
        return false
    }

    override fun rockTouchesFloor(rock: PositionedRock): Boolean {
        return rock.bottom < 0
    }

    override fun settleRock(rock: Rock, position: Point) {
        this.settleRock(PositionedRock(rock, position))
    }

    override fun settleRock(rock: PositionedRock) {
        if (rock.top + 1 > this.height) {
            for (i in 0 .. (rock.top - this.height)) {
                this.grid.add(MutableList(this.width.toInt()) { '.' })
            }
        }

        for (y in rock.bottom..rock.top) {
            for (x in rock.left..rock.right) {
                if (rock.hasElementAt(x, y)) {
                    this.grid[y.toInt()][x.toInt()] = '#'
                }
            }
        }
    }

    override fun prettyPrint(limit: Int?) {
        var g = this.grid.toList().reversed()
        g = limit?.let { g.take(limit) } ?: g
        g.forEach { println(it.joinToString(" ")) }
    }

}

class WindowedChamber(override val width: Long) : Chamber {
    var grid: MutableList<MutableList<Char>> = mutableListOf()
    private var _windowOffset = 0L

    val windowSize = 100
    val windowOffset: Long get() = this._windowOffset
    override val height: Long get() = this.grid.size.toLong() + this.windowOffset

    override fun rowAt(y: Long): List<Char> {
        if (y < windowOffset) {
            return listOf()
        }
        return this.grid[(y - windowOffset).toInt()].toList()
    }

    override fun takeTopRows(num: Int): List<List<Char>> {
        return this.grid.let {
            if (num > this.height) {
                it.toList()
            } else if (num > this.windowSize) {
                it.takeLast(this.windowSize)
            } else {
                it.takeLast(num)
            }
        }.map { it.toList() }
    }

    override fun contains(x: Long, y: Long): Boolean {
        return (x in  0 until this.width) && (y in this.windowOffset until this.height)
    }

    override fun hasSettledRockAt(x: Long, y: Long): Boolean {
        if (!this.contains(x, y)) {
            return false
        }

        val yOffset = y - this.windowOffset
        return grid[yOffset.toInt()][x.toInt()] == '#'
    }

    override fun rockTouchesWall(rock: PositionedRock): Boolean {
        return rock.left < 0 || rock.right >= this.width
    }

    override fun rockItersectsSettledRock(rock: PositionedRock): Boolean {
        for (y in rock.bottom..rock.top) {
            for (x in 0 until this.width) {
                if (this.hasSettledRockAt(x, y) && rock.hasElementAt(x, y)) {
                    return true
                }
            }
        }
        return false
    }

    override fun rockTouchesFloor(rock: PositionedRock): Boolean {
        return rock.bottom < this.windowOffset
    }

    override fun settleRock(rock: Rock, position: Point) {
        this.settleRock(PositionedRock(rock, position))
    }

    override fun settleRock(rock: PositionedRock) {
        if (rock.top < this.windowOffset || rock.bottom < this.windowOffset) {
            return
        }

        if (rock.top + 1 > this.height) {
            for (i in 0 .. (rock.top - this.height)) {
                this.grid.add(MutableList(this.width.toInt()) { '.' })
            }
        }

        for (y in rock.bottom..rock.top) {
            for (x in rock.left..rock.right) {
                if (rock.hasElementAt(x, y)) {
                    val yOffset = y - this.windowOffset
                    this.grid[yOffset.toInt()][x.toInt()] = '#'
                }
            }
        }

        val windowOverrun = this.grid.size - this.windowSize
        if (windowOverrun > 0) {
            this.grid = this.grid.drop(windowOverrun).toMutableList()
            this._windowOffset += windowOverrun
        }
    }

    override fun prettyPrint(limit: Int?) {
        var g = this.grid.toList().reversed()
        g = limit?.let {
            if (it > this.windowSize) {
                g
            } else {
                g.take(limit)
            }
        } ?: g
        g.forEach { println(it.joinToString(" ")) }
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
                Direction.LEFT,
                Direction.RIGHT -> {
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

data class DroppedRockEvent(
    val droppedRocks: Long,
    val jetPatternIndex: Int,
    val rockSequenceIndex: Int,
    val chamberHeight: Long,
)

class DropRocksIterator(
    val chamber: Chamber,
    val jetPattern: String,
    val rockSequence: List<Rock>,
    private val onDroppedRock: ((DroppedRockEvent, Chamber) -> Unit)? = null,
) {
    var rockSequenceCounter: Long = 1L
    var jetPatternCounter: Long = 0L
    var droppedRocksCounter: Long = 0L
    var currentDropRockIterator: DropRockIterator = DropRockIterator(rockSequence.first(), chamber)

    val fallingRock: PositionedRock get() = this.currentDropRockIterator.fallingRock

    private fun prepareNextRockToDrop() {
        this.droppedRocksCounter += 1
        val jetPatternIndex = this.jetPatternCounter % this.jetPattern.length
        val rockSequenceIndex = rockSequenceCounter % rockSequence.size

        onDroppedRock?.let { fn ->
            val event = DroppedRockEvent(
                this.droppedRocksCounter,
                jetPatternIndex.toInt(),
                rockSequenceIndex.toInt(),
                this.chamber.height,
            )
            fn(event, this.chamber)
        }

        val nextRock = rockSequence[rockSequenceIndex.toInt()]
        this.currentDropRockIterator = DropRockIterator(nextRock, chamber)
        this.rockSequenceCounter += 1
    }

    private fun takeNextDropRockStep(): Boolean {
        if (!currentDropRockIterator.hasNext()) {
            return true
        }

        val jetPatternIndex = this.jetPatternCounter % this.jetPattern.length
        val action = when (this.jetPattern[jetPatternIndex.toInt()]) {
            '>' -> DropRockAction.JET_PUSH_ROCK_RIGHT
            '<' -> DropRockAction.JET_PUSH_ROCK_LEFT
            else -> throw error("Invalid jet action at index ${jetPatternIndex}")
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

class DropRocksRunner(
    val jetPattern: String,
    val rockSequence: List<String>,
    val createChamberFn: () -> Chamber,
) {
    fun dropRocks(
        numRocks: Long,
        onDroppedRock: ((DroppedRockEvent, Chamber) -> Unit)? = null,
    ): Chamber {
        val chamber = this.createChamberFn()
        val iter = DropRocksIterator(
            chamber = chamber,
            jetPattern = this.jetPattern,
            rockSequence = this.rockSequence.map { Rock(it) },
            onDroppedRock,
        )

        (1..numRocks).forEach { step ->
            iter.dropNextRock()
        }

        return chamber
    }
}

class Canvas(
    val width: Int,
    val height: Int,
    defaultCellChar: Char = ' '
) {
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
            width = chamber.width.toInt() + 2,
            height = (fallingRock?.let { it.top.toInt() + 1 } ?: chamber.height.toInt()) + 1,
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
                    canvas.drawCell('#', row.toInt(), col.toInt())
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
                    canvas.drawCell('@', row.toInt(), col.toInt())
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

    override fun part1(jetPattern: ParsedInput): Any {
        val rockSequence = ROCK_SHAPES
        val runner = DropRocksRunner(jetPattern, rockSequence) { SimpleChamber(7) }
        val chamber = runner.dropRocks(2022)
        return chamber.height
    }

    override fun part2(jetPattern: ParsedInput): Any {
        val rockSequence = ROCK_SHAPES
        val runner = DropRocksRunner(jetPattern, rockSequence) { SimpleChamber(7) }

        val droppedRockEvents = mutableListOf<DroppedRockEvent>()
        val loopDetectionMap = mutableMapOf<Triple<String, Int, Int>, List<DroppedRockEvent>>()
        val loopDetectionHits = mutableMapOf<Triple<String, Int, Int>, Int>()

        runner.dropRocks(2000) { event, chamber ->
            droppedRockEvents.add(event)
            val rows = chamber.takeTopRows(10)
            val coverage = rows.fold(MutableList(chamber.width.toInt()) { '.' }) { l, r ->
                r.forEachIndexed { idx, c -> if (c == '#') l[idx] = '#' }
                l
            }

            if (coverage.all { it == '#' }) {
                val key = Triple(rows.toString(), event.jetPatternIndex, event.rockSequenceIndex)

                if (key in loopDetectionMap) {
                    val hitCount = loopDetectionHits.getOrDefault(key, 0)
                    loopDetectionHits[key] = hitCount + 1
                }

                val value = loopDetectionMap.getOrDefault(key, listOf())
                loopDetectionMap[key] = value + event
            }
        }

        val loopDetectionKey = if (loopDetectionHits.isNotEmpty()) loopDetectionHits.keys.first() else null
        val loopDetectionEvents = loopDetectionKey?.let { loopDetectionMap[it] }

        val (firstLoopDetectionEvent, droppedRocksDiff, chamberHeightDiff) = loopDetectionEvents
            ?.windowed(2)
            ?.first()
            ?.let { (evtA, evtB) ->
                val droppedRocksDiff = evtB.droppedRocks - evtA.droppedRocks
                val chamberHeightDiff = evtB.chamberHeight - evtA.chamberHeight
                Triple(evtA, droppedRocksDiff, chamberHeightDiff)
            } ?: throw error("No pattern detected")

        val rocksToDrop = 1_000_000_000_000L - firstLoopDetectionEvent.droppedRocks
        val intermediateChamberHeight = (rocksToDrop / droppedRocksDiff) * chamberHeightDiff + firstLoopDetectionEvent.chamberHeight
        val remainingRocksToDrop = rocksToDrop % droppedRocksDiff

        val remainingLoopDetectionEvt = droppedRockEvents
            .dropWhile { it != firstLoopDetectionEvent }
            .drop(1)
            .take(remainingRocksToDrop.toInt())
            .last()
        val remainingHeight = remainingLoopDetectionEvt.chamberHeight - firstLoopDetectionEvent.chamberHeight
        val finalChamberHeight = intermediateChamberHeight + remainingHeight

        return finalChamberHeight
    }
}

fun main() {
    Day17.solve(test = true)
}