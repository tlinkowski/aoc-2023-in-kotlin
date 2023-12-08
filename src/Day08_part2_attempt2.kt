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

    fun part2_partial(startNodeDir: NodeDir, lrs: CharArray,
                      nodeDirMap: Map<String, NodeDir>): Long {
        var currentNodeDir = startNodeDir
        var counter = 0L
        while (true) {
            for (lr in lrs) {
                currentNodeDir = nodeDirMap[if (lr == 'L') currentNodeDir.left else currentNodeDir.right]!!
                counter++
                if (currentNodeDir.isEnd) {
                    return counter
                }
            }
        }
    }

    fun part2(input: List<String>): Long {
        val lrs = input[0].toCharArray()

        val nodeDirs = input.drop(2)
            .map { it.parseNodeDirs() }

        val nodeDirMap = nodeDirs.associateBy { it.source }

        val results = nodeDirs
            .filter { it.isStart }
            .map { part2_partial(it, lrs, nodeDirMap) }

        println(results) // calculated LCM externally

        return 6
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
