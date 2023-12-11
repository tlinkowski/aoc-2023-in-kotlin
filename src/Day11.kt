import kotlin.math.abs

fun main() {
    val day = "Day11"

    // MODEL
    data class Point(val x: Int, val y: Int)

    data class Galaxy(val point: Point)

    data class Universe(val galaxies: List<Galaxy>)

    // PARSE
    fun List<String>.parseUniverse(): Universe = flatMapIndexed { y, line ->
        line.mapIndexedNotNull { x, c -> if (c == '#') Galaxy(Point(x, y)) else null }
    }.let { Universe(it) }

    // SOLVE
    fun Universe.absentXs() = galaxies.map { it.point.x }.toSet()
        .let { presentXs -> (0..presentXs.max()) - presentXs }

    fun Universe.absentYs() = galaxies.map { it.point.y }.toSet()
        .let { presentYs -> (0..presentYs.max()) - presentYs }

    fun Universe.expand(expansionFactor: Int): Universe {
        val absentXs = absentXs()
        val absentYs = absentYs()

        fun Point.expand() = Point(
            x = x + (expansionFactor - 1) * absentXs.count { it < x },
            y = y + (expansionFactor - 1) * absentYs.count { it < y }
        )

        return galaxies
            .map { Galaxy(it.point.expand()) }
            .let { Universe(it) }
    }

    fun <T> Collection<T>.combinations(): List<Pair<T, T>> = flatMapIndexed { i, a ->
        mapIndexedNotNull { j, b -> if (i < j) a to b else null }
    }

    fun Galaxy.distanceTo(o: Galaxy): Int = abs(point.x - o.point.x) + abs(point.y - o.point.y)

    fun part1(input: List<String>): Long {
        val universe = input.parseUniverse().expand(expansionFactor = 2)

        return universe.galaxies.combinations()
            .sumOf { (a, b) -> a.distanceTo(b).toLong() }
    }

    fun part2(input: List<String>): Long {
        val universe = input.parseUniverse().expand(expansionFactor = 1000000)

        return universe.galaxies.combinations()
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
