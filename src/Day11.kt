import kotlin.math.abs

fun main() {
    val day = "Day11"

    // MODEL
    data class Point(val x: Int, val y: Int)

    data class Galaxy(val id: Int, val point: Point)

    data class Universe(val galaxies: Map<Point, Galaxy>)

    // PARSE
    fun List<String>.parseUniverse(expansionFactor: Int): Universe {
        val emptyXs = this[0].indices
            .filter { x -> all { line -> line[x] == '.' } }
            .toSet()
        val emptyYs = indices
            .filter { y -> this[y].all { it == '.' } }
            .toSet()

        fun expandPoint(x: Int, y: Int) = Point(
            x + (expansionFactor - 1) * emptyXs.count { ex -> ex < x },
            y + (expansionFactor - 1) * emptyYs.count { ey -> ey < y }
        )

        var id = 0
        return flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, c -> if (c == '#') Galaxy(id++, expandPoint(x, y)) else null }
        }
            .associateBy { it.point }
            .let { Universe(it) }
    }

    // SOLVE

    fun <T> combinations(c: Collection<T>): List<Pair<T, T>> {
        return c.flatMapIndexed { i, a -> c.mapIndexedNotNull { j, b -> if (i < j) a to b else null } }

//        return c.flatMap { a -> c.mapNotNull { b -> if (a != b) setOf(a, b) else null } }.toSet()
    }

    fun Galaxy.distanceTo(o: Galaxy): Int = abs(point.x - o.point.x) + abs(point.y - o.point.y)

    fun part1(input: List<String>): Long {
        val universe = input.parseUniverse(2)

        return combinations(universe.galaxies.values)
            .sumOf { (a, b) -> a.distanceTo(b) }
            .toLong()
    }

    fun part2(input: List<String>): Long {
        val universe = input.parseUniverse(1000000)

        return combinations(universe.galaxies.values)
            .sumOf { (a, b) -> a.distanceTo(b).toLong() }
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 374L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 82000210L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
