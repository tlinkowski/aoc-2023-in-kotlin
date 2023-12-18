import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    val day = "Day18"

    // MODEL
    data class DigMove(val dir: Dir, val meters: Int, val color: String)

    data class DigPlan(val moves: List<DigMove>)

    // PARSE
    fun String.parseDigMove() = split(" ").let { (dir, meters, color) ->
        DigMove(
            Dir.entries.first { it.name.startsWith(dir) },
            meters = meters.toInt(),
            color = color.substring(2).dropLast(1)
        )
    }

    fun List<String>.parseDigPlan() = DigPlan(map { it.parseDigMove() })

    // SOLVE
    fun Char.toDir() = when (this) {
        '0' -> Dir.RIGHT
        '1' -> Dir.DOWN
        '2' -> Dir.LEFT
        '3' -> Dir.UP
        else -> throw RuntimeException()
    }

    fun DigMove.expand() = DigMove(
        dir = color.last().toDir(),
        meters = color.dropLast(1).toInt(radix = 16),
        color = color
    )

    fun DigPlan.expand() = DigPlan(moves.map { it.expand() })

    fun Point.nextCorner(move: DigMove) = move(move.dir, move.meters)

    fun DigPlan.findCornerPoints(start: Point) = moves.runningFold(start, Point::nextCorner)

    fun List<Point>.toNiceString(): String {
        data class Segment(val a: Point, val b: Point)

        operator fun Segment.contains(p: Point) = (p.x in a.x..b.x || p.x in b.x..a.x)
                && (p.y in a.y..b.y || p.y in b.y..a.y)

        val segments = zipWithNextCircular(::Segment)
        return toGridRange { it }.toNiceString { p ->
            if (segments.any { segment -> p in segment }) '#' else '.'
        }
    }

    fun DigPlan.digArea(print: Boolean = false): Long {
        val cornerPoints = findCornerPoints(Point(0, 0))
        if (print) println("Trench:\n" + cornerPoints.toNiceString())

        val innerArea = cornerPoints
            .zipWithNextCircular { a, b -> (a.y + b.y).toLong() * (a.x - b.x) }
            .sum() / 2 // trapezoid formula

        val perimeter = cornerPoints
            .zipWithNextCircular { a, b -> abs(a.x - b.x).toLong() + abs(a.y - b.y) }
            .sum()

        return abs(innerArea) + perimeter / 2 + 1 // Pick's theorem
    }

    fun part1(input: List<String>, print: Boolean = false): Long {
        val digPlan = input.parseDigPlan()
        return digPlan.digArea(print)
    }

    fun part2(input: List<String>): Long {
        val digPlan = input.parseDigPlan().expand()
        return digPlan.digArea()
    }

    // TESTS
    val test1a = part1(readInput("$day/test1a"), print = true)
    9L.let { check(test1a == it) { "Test 1a: is $test1a, should be $it" } }

    val test1 = part1(readInput("$day/test"), print = true)
    62L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    952408144115L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    62573L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
