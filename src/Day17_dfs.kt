fun main() {
    val day = "Day17"

    // MODEL
    data class City(val gridRange: GridRange, val map: Map<Point, Int>)

    // PARSE
    fun List<String>.parseCity() = City(
        toGridRange(),
        toPointMap().mapValues { (_, c) -> c.digitToInt() }
    )

    // SOLVE
    fun City.minHeatLoss(cur: Point, lastDirs: List<Dir>): Int? { // DFS
        if (cur !in gridRange) {
            return null
        }
        if (cur.x == gridRange.xRange.last && cur.y == gridRange.yRange.last) {
            return 0
        }

        println("$cur: $lastDirs")
        val filter = Dir.entries
            .let { dirs -> if (lastDirs.isEmpty()) dirs else dirs - lastDirs.last().opposite() }
            .filter { dir -> lastDirs.isEmpty() || !lastDirs.all { it == dir } }
        val mapNotNull = filter
            .mapNotNull { dir -> minHeatLoss(cur.move(dir), (lastDirs + dir).take(3)) }
        return mapNotNull
            .minOfOrNull {
                val aaaa = map[cur]!!
                aaaa + it
            }
    }

    fun part1(input: List<String>): Long {
        val city = input.parseCity()

        return city.minHeatLoss(Point(0, 0), listOf())!!.toLong()
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    102L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    0L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
