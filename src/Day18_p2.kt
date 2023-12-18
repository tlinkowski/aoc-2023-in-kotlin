import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    val day = "Day18"

    // MODEL
    data class DigMove(val dir: Dir, val m: Int, val color: String)

    data class DigPlan(val moves: List<DigMove>)

    data class DigPoint(val point: Point, val move: DigMove)

    // PARSE
    fun String.parseDigMove() = split(" ").let { (a, b, c) ->
        DigMove(
            Dir.entries.first { it.name.startsWith(a) },
            m = b.toInt(),
            color = c.substring(2).dropLast(1)
        )
    }

    fun List<String>.parseDigPlan() = DigPlan(map { it.parseDigMove() })

    // SOLVE
    fun DigMove.resolve() = DigMove(
        dir = when(color.last()) {
            '0' -> Dir.RIGHT
            '1' -> Dir.DOWN
            '2' -> Dir.LEFT
            '3' -> Dir.UP
            else -> throw RuntimeException()
        },
        m = Integer.parseInt(color.dropLast(1), 16),
        color = ""
    )

    fun DigPlan.findDigPoints(start: Point): List<DigPoint> {
        val digPoints = mutableListOf<DigPoint>()

        var cur = start
        for (move in moves) {
            cur = cur.move(move.dir, move.m)
            digPoints += DigPoint(cur, move)
        }

        return digPoints
    }

    fun DigPoint.move(dir: Dir) = copy(point = point.move(dir))

    fun List<DigPoint>.shifted(): List<DigPoint> {
        val points = toMutableList()
        println("Before")
        points.forEach { println(it) }
        for (i in points.indices) {
            val j = (i + 1) % points.size
            when (points[j].move.dir) {
                Dir.DOWN -> {
                    points[i] = points[i].move(Dir.RIGHT)
                    points[j] = points[j].move(Dir.RIGHT)
                }
                Dir.LEFT -> {
                    points[i] = points[i].move(Dir.DOWN)
                    points[j] = points[j].move(Dir.DOWN)
                }
                else -> {}
            }
        }
        println("After")
        points.forEach { println(it) }
        return points

//        return map { dp ->
//            var x = dp.point.x
//            var y = dp.point.y
//            if (none { it.point.y == dp.point.y && it.point.x > dp.point.x }) {
//                x++
//            }
//            if (none { it.point.x == dp.point.x && it.point.y > dp.point.y }) {
//                y++
//            }
//            DigPoint(Point(x, y), dp.move)
//        }
    }

    data class ModDigPoint(val dp: DigPoint, val pipe: Pipe)

    fun Point.isInside(loop: List<ModDigPoint>): Boolean {
        if (loop.any { move -> this == move.dp.point }) {
            return false
        }

        val sameYLowerX = loop.filter { it.dp.point.y == y && it.dp.point.x < x }

        val simpleVerticalIntersections = sameYLowerX
            .count { it.pipe == Pipe.VERTICAL }

        val edgeVerticalIntersections = sameYLowerX.asSequence()
            .filter { it.pipe != Pipe.HORIZONTAL }
            .sortedBy { it.dp.point.x }
            .map { it.pipe.symbol }
            .zipWithNext { a, b -> "$a$b" }
            .count { it in listOf("┌┘", "└┐") }

        return (simpleVerticalIntersections + edgeVerticalIntersections) % 2 == 1

    }

    fun toPipe(a: DigPoint, b: DigPoint): Pipe {
        return Pipe.entries.drop(1).first { a.move.dir.opposite() in it.dirs && b.move.dir in it.dirs }
    }

    fun toNiceString(gridRange: GridRange, loop: List<DigPoint>, interior: Set<Point>): String {
        val map = loop.associateBy { it.point }

        fun symbol(p: Point) = if (p in interior) '▒' else map[p]?.move?.dir?.symbol ?: ' '

        return gridRange.yRange.joinToString("\n") { y ->
            gridRange.xRange.map { x -> symbol(Point(x, y)) }.joinToString("")
        }
    }


    fun toNiceString(gridRange: GridRange, loop: List<ModDigPoint>, interior: Set<Point>): String {
        val map = loop.associateBy { it.dp.point }

        fun symbol(p: Point) = if (p in interior) '▒' else map[p]?.pipe?.symbol ?: ' '

        return gridRange.yRange.joinToString("\n") { y ->
            gridRange.xRange.map { x -> symbol(Point(x, y)) }.joinToString("")
        }
    }


    fun toGridRange(digPoints: List<DigPoint>): GridRange {
        val gridRange = GridRange(
            xRange = digPoints.minOf { it.point.x }..digPoints.maxOf { it.point.x },
            yRange = digPoints.minOf { it.point.y }..digPoints.maxOf { it.point.y }
        )
        return gridRange
    }

    fun toModDigPoints(digPoints: List<DigPoint>): List<ModDigPoint> {
        return digPoints.plus(digPoints.first()).zipWithNext { a, b -> ModDigPoint(a, toPipe(a, b)) }
    }

    fun detectInteriorPoints(gridRange: GridRange, modDigPoints: List<ModDigPoint>): Set<Point> {
        return gridRange.allPoints()
            .filter { it.isInside(modDigPoints) }
            .toSet()
    }

    fun DigPlan.area(): Long {
        moves.forEach { println(it) }

        val digPoints = findDigPoints(Point(0, 0))
            .shifted()
        println("Dig points: ${digPoints.size}")
        val gridRange = toGridRange(digPoints)
        println("Grid range: $gridRange")

        val circularDigPoints = digPoints + digPoints.first()
        circularDigPoints.forEach { println(it) }
        val area = circularDigPoints
            .zipWithNext { a, b -> (a.point.y + b.point.y).toLong() * (a.point.x - b.point.x).toLong() }
            .sum() / 2

//        val moves = digPoints
//            .map { it.move }
//        val downLeftLength = moves
//            .filter { it.dir == Dir.DOWN || it.dir == Dir.LEFT }
//            .sumOf { it.m }
//
//        val downLeftCorners = moves.zipWithNext()
//            .count { (a, b) -> a.dir == Dir.DOWN && b.dir == Dir.LEFT }

        return abs(area)// + downLeftLength + downLeftCorners
    }

    fun part1(input: List<String>): Long {
        val digPlan = input.parseDigPlan()
        return digPlan.area()
    }

    fun part2(input: List<String>): Long {
        val digPlan = DigPlan(input.map { it.parseDigMove().resolve() })
        return digPlan.area()
    }

    // TESTS
    val test2 = part1(readInput("$day/test2"))
    9L.let { check(test2 == it) { "Test 1a: is $test2, should be $it" } }

    val test1 = part1(readInput("$day/test"))
    62L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2a = part2(readInput("$day/test"))
    952408144115L.let { check(test2a == it) { "Test 2: is $test2a, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    62573L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
