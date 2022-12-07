package day07

// Original solution used to solve the AoC day 6 puzzle. Warts and all.

import common.Solution

typealias ParsedInput = List<String>

const val ROOT_PATH = "/"

fun pathfor(basepath: String, name: String): String {
    if (name == ROOT_PATH) return ROOT_PATH
    return if (basepath == ROOT_PATH) "$ROOT_PATH$name" else "$basepath/$name"
}

sealed class FSNode {

    abstract val name: String;
    abstract val path: String;

    val absolutepath: String
        get() = pathfor(path, name)

    data class File(override var name: String, override val path: String, val size: Int): FSNode()

    data class Dir(override val name: String, override val path: String, val items: Set<String> = setOf()): FSNode() {

        fun addNode(n: FSNode): Dir {
            return this.copy(items = this.items + n.name)
        }

        fun contains(n: FSNode): Boolean {
            return n.name in this.items
        }

        fun contains(n: String): Boolean {
            return n in this.items
        }

    }

}

typealias FSNodes = Map<String, FSNode>

fun buildFileSystemFromInput(input: List<String>): FSNodes {
    val root = FSNode.Dir(name = ROOT_PATH, path = ROOT_PATH)
    var fsnodes: FSNodes = mapOf<String, FSNode>(root.name to root)
    var dirStack = listOf<String>()
    var lines = input.toList()

    while (lines.isNotEmpty()) {
        val line = lines.first()
        lines = lines.drop(1)

        if (line[0] != '$') {
            break
        }

        val parts = line.split(" ")
        val (_, cmd) = parts

        when (cmd) {
            "cd" -> {
                when (val arg = parts.last()) {
                    ".." -> dirStack = dirStack.dropLast(1)
                    else -> dirStack += arg
                }
            }
            "ls" -> {
                var i = 0
                while (i < lines.size && lines[i][0] != '$') {
                    val (arg, name) = lines[i].split(" ")
                    val pwdpath = root.name + dirStack.drop(1).joinToString("/")
                    val nodepath = pathfor(pwdpath, name)

                    if (nodepath !in fsnodes) {
                        val node = when (arg) {
                            "dir" -> FSNode.Dir(name = name, path = pwdpath)
                            else -> FSNode.File(name = name, path = pwdpath, size = arg.toInt())
                        }
                        fsnodes += (node.absolutepath to node)
                        val currDir = fsnodes[pwdpath]
                        when (currDir) {
                            is FSNode.Dir -> fsnodes += (pwdpath to currDir.addNode(node))
                            else -> throw error("current directory is not a directory $currDir")
                        }
                    }
                    i += 1
                }
                lines = lines.drop(i)
            }
        }
    }

    return fsnodes
}

fun FSNodes.nodeSizeFor(path: String, cache: MutableMap<String, Long> = mutableMapOf()): Long {
    if (path in cache) {
        return cache[path]!!
    }
    val size = when (val node = this[path]!!) {
        is FSNode.Dir -> node.items.sumOf { this.nodeSizeFor(pathfor(path, it), cache) }
        is FSNode.File -> node.size.toLong()
        else -> throw error("Unknown node type")
    }
    cache[path] = size
    return size
}

fun FSNodes.prettyPrintTree(nodepath: String = "/", indent: Int = 0) {
    val node = this[nodepath]!!
    print((1..indent).map { ' ' }.joinToString("") + "- ${node.name} ")
    when (node) {
        is FSNode.Dir -> {
            println("(dir)")
            node.items.forEach { nodename ->
                this.prettyPrintTree("${if (nodepath == "/") "" else nodepath}/$nodename", indent + 4)
            }
        }
        is FSNode.File -> {
            println("(file, size = ${node.size})")
        }
        else -> throw error("Unknown node $nodepath")
    }
}

object Day07 : Solution.LinedInput<ParsedInput>(day = 7) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input
    }

    override fun part1(input: ParsedInput): Any {
        var fsnodes = buildFileSystemFromInput(input)

        val cache = mutableMapOf<String, Long>()
        return fsnodes.values
            .filterIsInstance<FSNode.Dir>()
            .map { (it.absolutepath to fsnodes.nodeSizeFor(it.absolutepath, cache)) }
            .filter { it.second <= 100000 }
            .sumOf { it.second }
    }

    override fun part2(input: ParsedInput): Any {
        var fsnodes = buildFileSystemFromInput(input)

        val fsDiskSpace = 70000000
        val requiredDiskSpace = 30000000
        val usedSpace = fsnodes.nodeSizeFor(ROOT_PATH)
        val avilableSpace = fsDiskSpace - usedSpace
        val minSpaceToFree = requiredDiskSpace - avilableSpace

        val cache = mutableMapOf<String, Long>()
        return fsnodes.values
            .filter { when (it) {
                is FSNode.Dir -> true
                else -> false
            } }
            .map { fsnodes.nodeSizeFor(it.absolutepath, cache) }
            .sortedBy { it }
            .dropWhile { it < minSpaceToFree }
            .first()
    }
}

fun main() {
    Day07.solve(test = false)
}