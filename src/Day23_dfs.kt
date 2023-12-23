fun main() {
    val day = "Day23"

    // MODEL
    data class Forest(
        val gridRange: GridRange, val start: Point, val end: Point,
        val trees: Set<Point>, val slopes: Map<Point, Dir>
    )

    // PARSE
    fun List<String>.parseForest(): Forest {
        val pointMap = toPointMap()
        val trees = pointMap.filterValues { it == '#' }.keys
        val slopes = pointMap.filterValues { it != '.' && it != '#' }.mapValues {
            when (it.value) {
                '^' -> Dir.UP
                '>' -> Dir.RIGHT
                'v' -> Dir.DOWN
                '<' -> Dir.LEFT
                else -> error(it)
            }
        }
        val start = pointMap.firstNotNullOf { (p, c) -> if (p.y == 0 && c == '.') p else null }
        val end = pointMap.firstNotNullOf { (p, c) -> if (p.y == size - 1 && c == '.') p else null }
        return Forest(toGridRange(), start, end, trees, slopes)
    }

    // SOLVE
    data class Hike(val last: Point, val visited: Set<Point>) {
        fun length() = visited.size
    }

//    fun Hike.move(p: Point) = Hike(last = p, visited = visited + p)
    fun Hike.tryMove(d: Dir): Hike? = last.move(d).let { p ->
        if (p in visited) null else Hike(p, visited + p)
    }

//        Hike(last = p, visited = visited + p)

    fun Forest.nextSteps(hike: Hike): List<Hike> {
        if (hike.last in slopes) {
            return listOfNotNull(hike.tryMove(slopes[hike.last]!!))
        }
        return Dir.entries
            .mapNotNull { hike.tryMove(it) }
            .filter { it.last !in trees }
            .flatMap { nextSteps(it) }
    }

    fun Forest.longestHike(): Hike {
        val startHike = Hike(start, setOf(start))
        val allHikes = nextSteps(startHike)

        return allHikes.maxBy { it.length() }
    }

    fun part1(input: List<String>): Int {
        val forest = input.parseForest()
        val longestHike = forest.longestHike()

        return longestHike.length()
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    94.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
