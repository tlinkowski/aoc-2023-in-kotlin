fun main() {
    val day = "Day21"

    // MODEL
    // remaining 64 steps
// He gives you an up-to-date map (your puzzle input) of his
// starting position (S), garden plots (.), and rocks (#)
    data class Garden(val gridRange: GridRange, val start: Point, val rocks: Set<Point>)

    // PARSE
    fun List<String>.parseGarden(): Garden {
        val pointMap = toPointMap()
        val start = pointMap.firstNotNullOf { (p, c) -> if (c == 'S') p else null }
        val rocks = pointMap.filterValues { it == '#' }.keys
        return Garden(toGridRange(), start, rocks)
    }

    // SOLVE
    fun Garden.reachablePlots(steps: Int): Int {
        var reachablePoints = setOf(start)
        for (step in 1..steps) {
            reachablePoints = reachablePoints
                .flatMap { p ->
                    Dir.entries
                        .map { d -> p.move(d) }
                        .filter { pp -> pp in gridRange && pp !in rocks }
                }
                .toSet()
            println("\nStep $step")
            println(gridRange.toNiceString {
                when (it) {
                    in reachablePoints -> 'O'
                    in rocks -> '#'
                    else -> '.'
                }
            })
        }
        return reachablePoints.size
    }

    fun part1(input: List<String>, steps: Int): Int {
        val garden = input.parseGarden()

        return garden.reachablePlots(steps)
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"), 6)
    16.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input, 64)
    3617.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
