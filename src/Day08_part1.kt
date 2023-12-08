fun main() {
    val day = "Day08"

    data class NodeDir(val source: String, val left: String, val right: String)

    fun String.parseNodeDirs() = NodeDir(
        source = substringBefore(" ="),
        left = substringAfter("(").substringBefore(", "),
        right = substringAfter(", ").removeSuffix(")")
    )

    fun part1(input: List<String>): Long {
        val lrs = input[0].toCharArray()

        val nodeDirs = input.drop(2)
            .map { it.parseNodeDirs() }

        val nodeDirMap = nodeDirs.associateBy { it.source }

        var currentNodeDir = nodeDirMap["AAA"]!!

        var iLoop = 0L
        var counter = 0L
        while (true) {
            if (++iLoop % 100000L == 0L) {
                println("Loop $iLoop: counter=$counter, currentNode=${currentNodeDir.source}")
            }
            for (lr in lrs) {
                currentNodeDir = nodeDirMap[if (lr == 'L') currentNodeDir.left else currentNodeDir.right]!!
                counter++
                if (currentNodeDir.source == "ZZZ") {
                    return counter
                }
            }
        }

//        return lrs.zip(nodeDirs)
//            .fold(nodeDirs[0].source) { node, (lr, nodeDir) ->
//                if (lr == 'L') nodeDir.left else nodeDir.right
//            }

    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test1"))
    check(part1 == 6L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 0L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
