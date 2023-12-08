enum class Turn { LEFT, RIGHT }

fun main() {
    val day = "Day08"

    // MODEL
    data class Branch(val node: String, val left: String, val right: String)

    data class DesertMap(val turns: List<Turn>, val nodeToBranchMap: Map<String, Branch>)

    // PARSE
    fun String.parseBranch() = Branch(
        node = substringBefore(" ="),
        left = substringAfter("(").substringBefore(", "),
        right = substringAfter(", ").removeSuffix(")")
    )

    fun List<String>.parseDesertMap() = DesertMap(
        turns = first().map { c -> Turn.entries.first { it.name.startsWith(c) } },
        nodeToBranchMap = drop(2).map { it.parseBranch() }.associateBy { it.node }
    )

    // COMPUTE
    fun Branch.nextNode(turn: Turn): String = when (turn) {
        Turn.LEFT -> left
        Turn.RIGHT -> right
    }

    fun DesertMap.nextBranch(branch: Branch, turn: Turn) = nodeToBranchMap[branch.nextNode(turn)]!!

    fun DesertMap.countLoops(startBranch: Branch, endNodeCondition: (String) -> Boolean): Int =
        generateSequence(startBranch) { branch -> turns.fold(branch, this::nextBranch) }
            .takeWhile { branch -> !endNodeCondition(branch.node) }
            .count()

    fun DesertMap.countSteps(startBranch: Branch, endNodeCondition: (String) -> Boolean): Int =
        turns.size * countLoops(startBranch, endNodeCondition)

    fun DesertMap.branch(node: String) = nodeToBranchMap[node]!!

    fun part1(input: List<String>): Int {
        val desertMap = input.parseDesertMap()
        val startBranch = desertMap.branch(node = "AAA")

        return desertMap.countSteps(startBranch) { node -> node == "ZZZ" }
    }

    fun DesertMap.branches() = nodeToBranchMap.values

    fun part2(input: List<String>): Long {
        val desertMap = input.parseDesertMap()
        val startBranches = desertMap.branches()
            .filter { branch -> branch.node.endsWith("A") }

        return startBranches
            .map { startBranch ->
                desertMap.countSteps(startBranch) { node -> node.endsWith("Z") }.toLong()
            }
            .reduce(::lcm)
    }

    // TESTS
    val part1 = part1(readInput("$day/test1"))
    check(part1 == 6) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 6L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
