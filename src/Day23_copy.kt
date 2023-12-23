fun main() {
    val day = "Day23"

    // MODEL
    data class Forest(
        val gridRange: GridRange, val start: Point, val end: Point,
        val trees: Set<Point>, val slopes: Map<Point, Dir>,
        val junctionPoints: Set<Point>
    )

    data class Graph(
        val start: Point,
        val end: Point,
        val neighbors: Map<Point, Set<Point>>,
        val lengths: Map<Set<Point>, Int>
    )

    // PARSE
    fun List<String>.parseForest(): Forest {
        val gridRange = toGridRange()
        val pointMap = toPointMap()
        val trees = pointMap.filterValues { it == '#' }.keys
        val slopes = pointMap.filterValues { it != '.' && it != '#' }.mapValues {
            when (it.value) {
                '^' -> Dir.UP
                '>' -> Dir.RIGHT
                'v' -> Dir.DOWN
                '<' -> Dir.LEFT
                else -> error(it)
            }
        }
        val start = pointMap.firstNotNullOf { (p, c) -> if (p.y == 0 && c == '.') p else null }
        val end = pointMap.firstNotNullOf { (p, c) -> if (p.y == size - 1 && c == '.') p else null }
        val junctions = pointMap.keys
            .filter { p -> Dir.entries.map { d -> p.move(d) }.count { mp -> mp in gridRange && mp !in trees } > 2 }
            .toSet()
        return Forest(gridRange, start, end, trees, slopes, junctions)
    }

    // SOLVE
    data class Hike(val cur: Point, val junctionTurns: Map<Point, Dir>, val length: Int)

    //    fun Hike.move(p: Point) = Hike(last = p, visited = visited + p)
//    fun Hike.tryMove(d: Dir): Hike? = cur.move(d).let { p ->
//        if (p in visited) null else Hike(p, cur, visited + p)
//    }

    fun Forest.neighbor(source: Point, dir: Dir): Pair<Point, Int>? {
        check(source in junctionPoints)
        var lastDir = dir
        var p = source.move(dir)
        var len = 1
        while (p != start && p != end && p !in junctionPoints) {
            val pair = Dir.entries
                .filter { it != lastDir.opposite() }
                .map { it to p.move(it) }
                .singleOrNull { it.second in gridRange && it.second !in trees }
                ?: return null
            lastDir = pair.first
            p = pair.second
            len++
        }

        return p to len
    }

    fun Forest.canWalk(p: Point) = p in gridRange && p !in trees

    fun Forest.toGraph(): Graph {
        val neighbors = mutableMapOf<Point, MutableSet<Point>>()
        val lengths = mutableMapOf<Set<Point>, Int>()

        for (j in junctionPoints) {
            Dir.entries.asSequence()
                .filter { dir -> canWalk(j.move(dir)) }
                .mapNotNull { dir -> neighbor(j, dir) }
                .forEach { (np, len) ->
                    neighbors.at(j) += np
                    neighbors.at(np) += j
                    lengths[setOf(j, np)] = len
                }
        }
        return Graph(start, end, neighbors, lengths)
    }

    fun Forest.nextJunction(hike: Hike, dir: Dir): Hike? {
        check(hike.cur == start || hike.cur in junctionPoints)
        var lastDir = dir
        var p = hike.cur.move(dir)
        var len = 1
        while (p != end && p !in junctionPoints) {
//            println(p)
            val pair = Dir.entries
                .filter { it != lastDir.opposite() }
                .map { it to p.move(it) }
                .singleOrNull { canWalk(it.second) }
                ?: return null
            lastDir = pair.first
            p = pair.second
            len++
        }
        if (p in hike.junctionTurns) {
            return null
        }

        val junctionTurn = hike.cur to dir
        return Hike(cur = p, junctionTurns = hike.junctionTurns + junctionTurn, length = hike.length + len)
    }

//    fun Forest.nextJunctionsSimple(p: Point): List<Pair<Point, Int>> {
//        check(p in junctionPoints)
//
//        return Dir.entries
//            .filter { dir -> p.move(dir).let { pp -> pp in gridRange && pp !in trees } }
//            .mapNotNull { dir -> nextSignificantPoint(p, dir) }
//    }

    fun Forest.nextJunctions(hike: Hike): List<Hike> {
        if (hike.cur == end) {
            return listOf(hike)
        }

        check(hike.cur in junctionPoints)

        return Dir.entries
            .filter { dir -> hike.cur.move(dir).let { p -> p in gridRange && p !in trees } }
            .mapNotNull { dir -> nextJunction(hike, dir) }
    }

//        Hike(last = p, visited = visited + p)
//
//    fun Forest.nextSteps(hike: Hike): List<Hike> {
//        if (hike.cur == end) {
//            return listOf(hike)
//        }
////        if (hike.last in slopes) {
////            return listOfNotNull(hike.tryMove(slopes[hike.last]!!))
////        }
//        return Dir.entries
//            .mapNotNull { hike.tryMove(it) }
//            .filter { it.cur in gridRange && it.cur !in trees }
//    }

    fun Forest.toNiceString(hike: Hike): String {
        return gridRange.toNiceString { p ->
            when (p) {
                in trees -> '#'
                in hike.junctionTurns -> hike.junctionTurns[p]!!.symbol
//                in slopes -> slopes[p]!!.symbol
                in junctionPoints -> 'x'
                else -> '.'
            }
        }
    }

    //    fun Forest.longestHikeMapNotWorking(): Hike {
//        val startHike = Hike(start, setOf(start))
//        var hikes = mapOf(start to listOf(startHike))
//        while (!hikes.values.asSequence().flatten().all { it.last == end }) {
////            if (hikes.size < 10) println(hikes)
//            println(hikes.size)
//            hikes = hikes.values.asSequence().flatten()
//                .flatMap { nextSteps(it) }
//                .groupBy { it.last }
//                .mapValues { (_, v) ->
//                    val maxLength = v.maxOf { it.length() }
//                    v.filter { it.length() == maxLength }
//                }
//        }
//
//        val longest = hikes.values.asSequence().flatten().maxBy { it.length() }
//        println(toNiceString(longest))
//        return longest
//    }
//
//    fun Forest.longestHikeSimpleNotWorking(): Hike {
//        val startHike = Hike(start, setOf(start))
//        var hikes = listOf(startHike)
//        while (!hikes.all { it.last == end }) {
////            if (hikes.size < 10) println(hikes)
//            println(hikes.size)
//            hikes = hikes.flatMap { nextSteps(it) }
//        }
//
//        val longest = hikes.maxBy { it.length() }
//        println(toNiceString(longest))
//        return longest
//    }

//    data class Result(val p: Point, val minDistance: Int)
//
//    class DijkstraResultStorage {
//
//        private val visited: MutableSet<Point> = mutableSetOf()
//        private val distances: MutableMap<Point, Int> = mutableMapOf()
//        private val unvisitedQueue: PriorityQueue<Result> = PriorityQueue(compareBy { it.minDistance })
//
//        fun popUnvisitedResult(): Result = unvisitedQueue.poll()!!.also { (p) -> visited += p }
//
//        fun distanceAt(point: Point) = distances[point]!!
//
//        fun isVisited(p: Point) = p in visited
//
//        fun register(result: Result) {
//            val p = result.p
//            val oldDistance = distances[p]
//
//            if (oldDistance == null || oldDistance > result.minDistance) {
//                distances[p] = result.minDistance
//                println("$p: ${result.minDistance}")
//                if (!isVisited(p)) fixUnvisitedQueue(result, oldDistance)
//            }
//        }
//
//        private fun fixUnvisitedQueue(result: Result, oldDistance: Int?) {
//            if (oldDistance != null) unvisitedQueue -= Result(result.p, oldDistance)
//            unvisitedQueue += result
//        }
//    }
//
//    fun Forest.startResult() = Result(start, 0)
//
//    fun Forest.longestHikeDijkstra(): Int {
//        val resultStorage = DijkstraResultStorage()
//        resultStorage.register(startResult())
//
//        while (!resultStorage.isVisited(end)) { // Dijkstra's algorithm (BFS + priority queue)
//            val curResult = resultStorage.popUnvisitedResult()
//
//            Dir.entries
//                .map { curResult.p.move(it) }
//                .filter { it in gridRange && it !in trees && !resultStorage.isVisited(it) }
//                .map { Result(it, curResult.minDistance - 1) }
//                .forEach(resultStorage::register)
//        }
//
//        return -resultStorage.distanceAt(end)
//    }

//    fun Forest.filterSamePointHikes(samePointHikes: List<Hike>): List<Hike> {
////        val maxLength = samePointHikes.maxOf { it.length() }
////        val filtered = samePointHikes.filter { it.length() == maxLength }
////        if (filtered.size > 1) {
////            filtered.forEach { h -> println("\n" + toNiceString(h))}
////            println("\n\n\n")
////        }
//        return samePointHikes.distinctBy { h -> h.length() to h.prev }
//    }

    data class GHike(val cur: Point, val visited: Set<Point>, val length: Int)

    fun Graph.nextSteps(hike: GHike): List<GHike> {
        if (hike.cur == end) {
            return listOf(hike)
        }
        return neighbors[hike.cur]!!
            .filter { it !in hike.visited }
            .map { GHike(it, hike.visited + it, hike.length + lengths[setOf(hike.cur, it)]!!)}
    }

    fun Forest.toNiceString(hike: GHike): String {
        return gridRange.toNiceString { p ->
            when (p) {
                in trees -> '#'
                in hike.visited -> 'x'
//                in slopes -> slopes[p]!!.symbol
//                in junctionPoints -> 'x'
                else -> '.'
            }
        }
    }

    fun Graph.longestHikeMap(): GHike {
        val startHike = GHike(start, setOf(start), 0)
        var hikes = mapOf(start to listOf(startHike))
        while (!hikes.values.asSequence().flatten().all { it.cur == end }) {
//            if (hikes.size < 10) println(hikes)
            println(hikes.values.sumOf { it.size })
            hikes = hikes.values.asSequence().flatten()
                .flatMap { nextSteps(it) }
                .groupBy { it.cur }
                .mapValues { (_, v) ->
                    val maxLength = v.maxOf { it.length }
                    v.filter { it.length == maxLength }
                }
        }

        val longest = hikes.values.asSequence().flatten().maxBy { it.length }
//        println(toNiceString(longest))
        return longest
    }

    fun Graph.longestHike(): GHike {
        val startHike = GHike(start, setOf(start), 0)
        var hikes = listOf(startHike)
        while (!hikes.all { it.cur == end }) {
//            if (hikes.size < 10) println(hikes)
            println(hikes.size)
            hikes = hikes.flatMap { nextSteps(it) }
        }

        val longest = hikes.maxBy { it.length }
//        println(toNiceString(longest))
        return longest
    }

    fun Forest.longestHike(): Hike {
        val startHike = Hike(start, mapOf(), 0)
        println("firstJunction")
        val firstJunction = nextJunction(startHike, Dir.DOWN)

        println("loop")
        var hikes = listOfNotNull(firstJunction)
        while (!hikes.all { it.cur == end }) {
            println(hikes.size)
            hikes = hikes.flatMap { nextJunctions(it) }
        }

        val longest = hikes.maxBy { it.length }
        println(toNiceString(longest))
        return longest
    }

    fun part2(input: List<String>): Int {
        val forest = input.parseForest()
        val longestHike = forest.toGraph().longestHike()

        return longestHike.length
    }

    // TESTS
//    val test1 = part1(readInput("$day/test"))
//    94.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    154.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
//    val part1 = part1(input)
//    2086.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2") // 4794 too low
}
