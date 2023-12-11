import kotlin.math.abs

fun main() {
    val day = "Day11"

    // MODEL
    data class Point(val x: Int, val y: Int)

    data class Galaxy(val id: Int, val point: Point)

    data class Universe(val galaxies: Map<Point, Galaxy>)

    // PARSE
    fun List<String>.expandUniverseX(): List<String> {
        val doubleX = this[0].indices
            .filter { x -> all { line -> line[x] == '.' } }
            .toSet()

        return map { line ->
            line.mapIndexed { x, c -> if (x in doubleX) "$c$c" else "$c" }.joinToString("")
        }
    }

    fun List<String>.expandUniverseY(): List<String> {
        return flatMap { line ->
            if (line.all { it == '.' }) listOf(line, line) else listOf(line)
        }
    }

    fun List<String>.expandUniverse() = expandUniverseX().expandUniverseY()

    fun List<String>.parseUniverse(): Universe {
        var id = 0
        return flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, c -> if (c == '#') Galaxy(id++, Point(x, y)) else null }
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
        val expandUniverse = input.expandUniverse()
//        expandUniverse.onEach(::println)
        val universe = expandUniverse.parseUniverse()

        return combinations(universe.galaxies.values)
            .sumOf { (a, b) -> a.distanceTo(b) }
            .toLong()
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 374L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 0L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
