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
    fun DigPlan.findDigPoints(start: Point): List<DigPoint> {
        val digPoints = mutableListOf<DigPoint>()

        var cur = start
        for (move in moves) {
            cur = generateSequence(cur) { it.move(move.dir) }
                .drop(1)
                .take(move.m)
                .onEach { digPoints += DigPoint(it, move) }
                .last()
        }

        return digPoints
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



    fun part1(input: List<String>): Long {
        val digPlan = input.parseDigPlan()
        val digPoints = digPlan.findDigPoints(Point(0, 0))
        val gridRange = GridRange(
            xRange = digPoints.minOf { it.point.x }..digPoints.maxOf { it.point.x } + 1,
            yRange = digPoints.minOf { it.point.y }..digPoints.maxOf { it.point.y } + 1
        )
        val modDigPoints = digPoints.plus(digPoints.first()).zipWithNext { a, b -> ModDigPoint(a, toPipe(a, b))}

        val interiorPoints = gridRange.allPoints()
            .filter { it.isInside(modDigPoints) }
            .toSet()

        println(toNiceString(gridRange, modDigPoints, interiorPoints))

        return digPoints.size.toLong() + interiorPoints.size.toLong()
        // 64264 is wrong
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    62L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    62573L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
