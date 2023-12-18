import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    val day = "Day18"

    // MODEL
    data class DigMove(val dir: Dir, val meters: Int, val color: String)

    data class DigPlan(val moves: List<DigMove>)

    data class CornerPoint(val point: Point, val sourceMove: DigMove)

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

    fun CornerPoint.move(move: DigMove) = CornerPoint(point.move(move.dir, move.meters), move)

    fun DigPlan.findCornerPoints(start: Point) = moves.dropLast(1)
        .runningFold(CornerPoint(start, moves.last()), CornerPoint::move)

    fun CornerPoint.offset(dir: Dir) = copy(point = point.move(dir))

    // without offsetting, the area would not include bottom-right points
    fun List<CornerPoint>.offsetTwoSides(): List<CornerPoint> {
        val points = toMutableList()
        for (i in points.indices) {
            val j = (i + 1) % points.size
            val offsetDir = when (points[j].sourceMove.dir) {
                Dir.DOWN -> Dir.RIGHT
                Dir.LEFT -> Dir.DOWN
                else -> null
            }
            if (offsetDir != null) {
                points[i] = points[i].offset(offsetDir)
                points[j] = points[j].offset(offsetDir)
            }
        }
        return points
    }

    fun List<CornerPoint>.toNiceString(): String {
        data class Segment(val a: Point, val b: Point, val dir: Dir)

        operator fun Segment.contains(p: Point) = p != b // end is exclusive
                && (p.x in a.x..b.x || p.x in b.x..a.x)
                && (p.y in a.y..b.y || p.y in b.y..a.y)

        val segments = zipWithNextCircular { a, b -> Segment(a.point, b.point, b.sourceMove.dir) }
        fun symbol(p: Point) = segments.firstOrNull { segment -> p in segment }
            ?.let { if (p == it.a) it.dir.symbol else '#' }
            ?: '.'

        return toGridRange { it.point }.toNiceString(::symbol)
    }

    fun DigPlan.digArea(print: Boolean = false): Long {
        val cornerPoints = findCornerPoints(Point(0, 0))
        if (print) println("Before offset:\n" + cornerPoints.toNiceString())

        val offsetPoints = cornerPoints.offsetTwoSides()
        if (print) println("After offset:\n" + offsetPoints.toNiceString())

        val area = offsetPoints
            .zipWithNextCircular { a, b -> (a.point.y + b.point.y).toLong() * (a.point.x - b.point.x).toLong() }
            .sum() / 2 // trapezoid formula

        return abs(area)
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
