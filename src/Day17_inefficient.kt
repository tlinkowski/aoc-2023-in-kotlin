import kotlin.math.abs

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
    data class Moment(val p: Point, val heatLoss: Long, val path: List<Dir>, val visited: Set<Point>)

    fun Moment.plusVisited(v: Set<Point>) = copy(visited = visited + v)

    fun City.tryMove(m: Moment, dir: Dir): Moment? = m.p.move(dir)
        .takeIf { it in gridRange && it !in m.visited }
        ?.let {
            Moment(
                it,
                m.heatLoss + map[it]!!,
                m.path + dir,
                m.visited + it
            )
        }

    fun GridRange.endpoint() = Point(xRange.last, yRange.last)

    fun City.isEndpoint(p: Point) = p == gridRange.endpoint()

    fun City.print(moment: Moment) {
//        println(moment.path)
        val dirs = moment.path.reversed()
        val points = moment.path.reversed()
            .runningFold(moment.p) { p, dir -> p.move(dir.opposite()) }
        val dirMap = points.dropLast(1).zip(dirs).toMap()
        gridRange.yRange.forEach { y ->
            gridRange.xRange.forEach { x ->
                val p = Point(x, y)
                print(dirMap[p]?.symbol ?: map[p]!!)
            }
            println()
        }
    }

    data class LastStateKey(val last: Dir, val lastSameCount: Int)

    fun List<Dir>.toKey() = LastStateKey(last(), takeLast(3).reversed().takeWhile { it == last() }.count())

    fun Point.hasDistFromStart(target: Int) = x + y == target

    fun City.nextDiagonal(input: Collection<Moment>, targetDist: Int): Collection<Moment> {
        var cur = input

        do {
            cur = cur.flatMap { c ->
                if (c.p.hasDistFromStart(targetDist)) {
                    listOf(c)
                } else {
                    val nextDirs = Dir.entries
                        .let { dirs -> if (c.path.isEmpty()) dirs else dirs - c.path.last().opposite() }
                        .filter { dir -> c.path.size < 3 || !c.path.takeLast(3).all { it == dir } }
                    nextDirs.mapNotNull { tryMove(c, it) }
                }
            }
                .groupBy { it.p to it.path.toKey() }
                .mapValues { (_, v) ->
                    v.minBy { it.heatLoss }
//                        .let { minV -> v.fold(minV) { m, v1 -> m.plusVisited(v1.visited) } }
                }
                .values
        } while (!cur.all { it.p.hasDistFromStart(targetDist) })

        return cur
    }

    fun City.minHeatLoss(start: Point): Long { // BFS
        var cur: Collection<Moment> = listOf(Moment(start, 0, listOf(), setOf()))

        val maxTarget = with(gridRange.endpoint()) { x + y }
        for (i in 1..maxTarget) {
            println("Diagonal $i/$maxTarget, input size=${cur.size}")
            cur = nextDiagonal(cur, i)
        }

        return cur.minOf { it.heatLoss }
    }

    fun part1(input: List<String>): Long {
        val city = input.parseCity()

        return city.minHeatLoss(Point(0, 0))
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
