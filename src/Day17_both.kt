import kotlin.math.abs

fun main() {
    val day = "Day17"

    // MODEL

    fun GridRange.endpoint() = Point(xRange.last, yRange.last)

    data class City(val gridRange: GridRange, val map: Map<Point, Int>) {
        val halfwayCount = with(gridRange.endpoint()) { x + y } / 2
    }

    // PARSE
    fun List<String>.parseCity() = City(
        toGridRange(),
        toPointMap().mapValues { (_, c) -> c.digitToInt() }
    )

    // SOLVE
    data class Moment(val p: Point, val heatLoss: Long, val path: List<Dir>, val visited: Set<Point>)

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

//    fun City.minExtraHeatLoss(m: Moment) = gridRange.endpoint().let {
//        abs(m.p.x - it.x) + abs(m.p.y - it.y)
//    }
//
//    fun City.maxHeatLoss(m: Moment) = generateSequence(m) { tryMove(it, Dir.DOWN) }
//        .last()
//        .let { m2 -> generateSequence(m2) { tryMove(it, Dir.RIGHT) } }
//        .last()
//        .heatLoss

    fun City.isHalfway(p: Point) = p.x + p.y == halfwayCount

    fun City.findAllHalfway(start: Point): List<Moment> {
        var cur = listOf(Moment(start, 0, listOf(), setOf()))

        do {
            println("Cur count ${cur.size}")
//            println("First cur p=${cur[0].p}, loss=${cur[0].heatLoss}")
            cur = cur.flatMap { c ->
                if (isHalfway(c.p)) {
                    listOf(c)
                } else {
                    val nextDirs = Dir.entries
                        .let { dirs -> if (c.path.isEmpty()) dirs else dirs - c.path.last().opposite() }
                        .filter { dir -> c.path.size < 3 || !c.path.takeLast(3).all { it == dir } }
                    nextDirs.mapNotNull { tryMove(c, it) }
                }
            }
                .groupBy { it.p to it.path.toKey() }
                .mapValues { (_, v) -> v.minBy { it.heatLoss } }
                .values
                .toList()
        } while (!cur.all { isHalfway(it.p) })

        return cur
    }

    fun areOK(fromStart: List<Dir>, fromEnd: List<Dir>): Boolean {
        val cont = fromStart + fromEnd.reversed().map { it.opposite() }
        return cont.windowed(3).none { (a, b, c) -> a == b && b == c }
    }

    fun City.minHeatLoss(): Long { // BFS
        val fromStart = findAllHalfway(Point(0, 0)).groupBy { it.p }
        val fromEnd = findAllHalfway(gridRange.endpoint()).groupBy { it.p }
        return fromStart.keys.intersect(fromEnd.keys)
            .flatMap { p -> fromStart[p]!!.flatMap { s -> fromEnd[p]!!.map { e -> s to e } } }
            .filter { (s, e) -> areOK(s.path.takeLast(3), e.path.takeLast(3)) }
            .minOf { (s, e) -> s.heatLoss + e.heatLoss }
    }

    fun part1(input: List<String>): Long {
        val city = input.parseCity()

        return city.minHeatLoss()
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
