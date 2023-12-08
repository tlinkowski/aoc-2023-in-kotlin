fun main() {
    val day = "Day08"

    data class NodeDir(val source: String, val left: String, val right: String) {
        val isStart = source.endsWith("A")
        val isEnd = source.endsWith("Z")
    }

    fun String.parseNodeDirs() = NodeDir(
        source = substringBefore(" ="),
        left = substringAfter("(").substringBefore(", "),
        right = substringAfter(", ").removeSuffix(")")
    )

    fun part2(input: List<String>): Long {
        val lrs = input[0].toCharArray()

        val nodeDirs = input.drop(2)
            .map { it.parseNodeDirs() }

        val nodeDirMap = nodeDirs.associateBy { it.source }

        val currentNodeDirs = nodeDirs
            .filter { it.isStart }
            .toMutableList()
        println("Start: ${currentNodeDirs.map { it.source }}")

        var iLoop = 0L
        var counter = 0L
        while (true) {
            if (++iLoop % 100000L == 0L) {
                println("Loop $iLoop: counter=$counter, currentNodes=${currentNodeDirs.map { it.source }}")
            }
            for (lr in lrs) {
                counter++
                currentNodeDirs.replaceAll {
                    nodeDirMap[if (lr == 'L') it.left else it.right]!!
                }
                if (currentNodeDirs.all { it.isEnd }) {
                    return counter
                }
            }
        }

    }
//
//    fun part2(input: List<String>): Long {
//        return 0
//    }

    // TESTS
//    val part1 = part1(readInput("$day/test1"))
//    check(part1 == 6L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 6L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
//    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
