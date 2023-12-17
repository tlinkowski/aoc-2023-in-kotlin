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
    data class Key(val p: Point, val lastDir: Dir?, val lastDirCount: Int)

    data class Moment(val k: Key, val visited: Set<Point>)

    fun City.tryMove(m: Moment, dir: Dir): Key? = dir
        .takeIf { d -> d != m.k.lastDir?.opposite()}
        .takeIf { d -> if (d == m.k.lastDir) m.k.lastDirCount < 10 else m.k.lastDir == null || m.k.lastDirCount >= 4 }
        ?.let { d -> m.k.p.move(d) }
        ?.takeIf { it in gridRange && it !in m.visited }
        ?.let {
            Key(
                it,
                dir,
                if (dir == m.k.lastDir) m.k.lastDirCount + 1 else 1
            )
        }

    fun GridRange.endpoint() = Point(xRange.last, yRange.last)

    fun City.isEndpoint(p: Point) = p == gridRange.endpoint()

    fun City.minHeatLoss(start: Point, cache: MutableMap<Key, Long>): Long { // BFS
        val startKey = Key(start, null, 0)
        cache[startKey] = 0
        var curMoments = listOf(Moment(startKey, setOf(start)))

        do {
            println("Cur count ${curMoments.size}")
            curMoments = curMoments.flatMap { m ->
                if (isEndpoint(m.k.p)) {
                    listOf(m)
                } else {
                    val refMinHeatLoss = cache[m.k]!!
                    val nextKeys = Dir.entries
                        .mapNotNull { dir -> tryMove(m, dir) }
                        .filter { nk ->
                            val newMinHeatLoss = refMinHeatLoss + map[nk.p]!!
                            val oldMinHeatLoss = cache[nk]
                            if (oldMinHeatLoss == null || newMinHeatLoss < oldMinHeatLoss) {
                                cache[nk] = newMinHeatLoss
                                true
                            } else {
                                false
                            }
                        }
                    nextKeys.map { Moment(it, m.visited + m.k.p)}
                }
            }//.distinctBy { it.k }
        } while (!curMoments.all { isEndpoint(it.k.p) })

        return curMoments.minOf { cache[it.k]!! }
    }

    fun part2(input: List<String>): Long {
        val city = input.parseCity()

        println("Input grid: ${city.gridRange}")
        return city.minHeatLoss(Point(0, 0), mutableMapOf())
    }

    // TESTS
//    val test1 = part1(readInput("$day/test"))
//    102L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    94L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
//    val part1 = part1(input)
//    1263L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
