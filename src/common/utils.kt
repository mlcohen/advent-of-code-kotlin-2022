package common

import java.io.File

fun readInputLines(filename: String) = File(filename).readLines()

fun readInputGroupedLines(filename: String): List<List<String>> {
    return File(filename)
        .readLines()
        .fold(mutableListOf(mutableListOf<String>())) { groups, line ->
        if (line.isEmpty()) {
            groups.add(mutableListOf())
        } else {
            groups.last().add(line)
        }
        groups
    }
}