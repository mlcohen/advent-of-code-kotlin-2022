package day07.solution2

// Refactored original solution. This is what I was originally trying to do but ended up getting
// lost creating a virtual file system in a purely functional way. Also greatly simplified the
// logic to process the input.

import common.Solution

typealias ParsedInput = List<String>

const val ROOT_PATH = "/"

fun pathfor(basepath: String, name: String): String {
    if (name == ROOT_PATH) return ROOT_PATH
    return if (basepath == ROOT_PATH) "$ROOT_PATH$name" else "$basepath/$name"
}

sealed class FSNode {
    abstract val name: String;
    abstract val parent: String;

    val absolutepath: String
        get() = pathfor(parent, name)

    data class File(
        override var name: String,
        override val parent: String,
        val size: Long,
        ): FSNode()

    data class Dir(
        override val name: String,
        override val parent: String,
        val items: Set<String> = setOf()
    ): FSNode() {
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

data class FileSystem(
    val nodes: Map<String, FSNode> = mapOf(ROOT_PATH to FSNode.Dir(ROOT_PATH, "")),
    val dirstack: List<String> = listOf()
) {
    fun currentWorkingDirectoryPath(): String {
        return if (dirstack.size == 0) {
            ""
        } else if (dirstack.size == 1) {
            dirstack.first()
        } else {
            "${ROOT_PATH}${dirstack.drop(1).joinToString("/")}"
        }
    }

    fun absolutePathFor(nodename: String): String {
        val cwpath = this.currentWorkingDirectoryPath()
        return "${if (cwpath == ROOT_PATH) "" else cwpath}/$nodename"
    }

    fun changeCurrentWorkingDirectory(dirname: String): FileSystem {
        return if (dirname == "..") {
            this.copy(dirstack = dirstack.dropLast(1))
        } else {
            this.copy(dirstack = dirstack + dirname)
        }
    }

    fun makeDirectory(dirname: String): FileSystem {
        val cwdpath = this.currentWorkingDirectoryPath()
        val nodepath = this.absolutePathFor(dirname)

        if (nodepath in this.nodes) return this

        val dir = FSNode.Dir(dirname, cwdpath)
        return when (val cwdir = this.nodes[cwdpath]) {
            is FSNode.Dir -> this.copy(nodes = nodes + (cwdpath to cwdir.addNode(dir)) + (nodepath to dir))
            else -> throw error("Can not upsert directory. current work directory is not a directory $cwdpath")
        }
    }

    fun makeFile(filename: String, size: Long): FileSystem {
        val cwdpath = this.currentWorkingDirectoryPath()
        val nodepath = this.absolutePathFor(filename)

        if (nodepath in this.nodes) return this

        val file = FSNode.File(filename, cwdpath, size)
        return when (val cwdir = this.nodes[cwdpath]) {
            is FSNode.Dir -> this.copy(nodes = nodes + (cwdpath to cwdir.addNode(file)) + (nodepath to file))
            else -> throw error("Can not upsert file. current work directory is not a directory $cwdpath")
        }
    }

    fun prettyPrintTree(nodepath: String = "/", indent: Int = 0) {
        val node = this.nodes[nodepath]!!

        print("${"".padStart(indent)}- ${node.name} ")
        when (node) {
            is FSNode.Dir -> {
                println("(dir)")
                node.items.forEach { nodename ->
                    this.prettyPrintTree("${pathfor(node.absolutepath, nodename)}", indent + 4)
                }
            }
            is FSNode.File -> {
                println("(file, size = ${node.size})")
            }
        }
    }

    fun nodeSizeFor(path: String): Long {
        val cache = mutableMapOf<String, Long>()
        val fs = this

        fun wrapper(p: String): Long {
            if (p in cache) {
                return cache[p]!!
            }

            val size = when (val node = fs.nodes[p]!!) {
                is FSNode.Dir -> node.items.sumOf { wrapper(pathfor(p, it)) }
                is FSNode.File -> node.size
            }
            cache[p] = size
            return size
        }

        return wrapper(path)
    }

    companion object {
        fun buildFromInput(input: List<String>): FileSystem {
            return input.fold(FileSystem()) { fs, line ->
                val parts = line.split(" ")
                val (a, b) = parts
                if (a == "$" && b == "cd") {
                    fs.changeCurrentWorkingDirectory(parts.last())
                } else if (a == "dir") {
                    fs.makeDirectory(b)
                } else if ("""\d+""".toRegex().matches(a)) {
                    fs.makeFile(parts.last(), a.toLong())
                } else {
                    fs
                }
            }
        }
    }
}

object Day07 : Solution.LinedInput<ParsedInput>(day = 7) {

    override fun parseInput(input: List<String>): ParsedInput {
        return input
    }

    override fun part1(input: ParsedInput): Any {
        val fs = FileSystem.buildFromInput(input)

        return fs.nodes.values
            .filterIsInstance<FSNode.Dir>()
            .map { (it.absolutepath to fs.nodeSizeFor(it.absolutepath)) }
            .filter { it.second <= 100000 }
            .sumOf { it.second }
    }

    override fun part2(input: ParsedInput): Any {
        val fs = FileSystem.buildFromInput(input)

        val fsDiskSpace = 70000000
        val requiredDiskSpace = 30000000
        val usedSpace = fs.nodeSizeFor(ROOT_PATH)
        val avilableSpace = fsDiskSpace - usedSpace
        val minSpaceToFree = requiredDiskSpace - avilableSpace

        return fs.nodes.values
            .filterIsInstance<FSNode.Dir>()
            .map { fs.nodeSizeFor(it.absolutepath) }
            .sortedBy { it }
            .dropWhile { it < minSpaceToFree }
            .first()
    }
}

fun main() {
    Day07.solve(test = false)
}