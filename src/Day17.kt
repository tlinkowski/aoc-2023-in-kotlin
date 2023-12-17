import java.util.*

fun main() {
    val day = "Day17"

    // MODEL
    class City(val gridRange: GridRange, val heatLossMap: Map<Point, Int>)

    data class Origin(val dir: Dir?, val dirCount: Int)

    data class Key(val point: Point, val origin: Origin)

    data class Result(val key: Key, val minHeatLoss: Int)

    // PARSE
    fun List<String>.parseCity() = City(
        toGridRange(),
        toPointMap().mapValues { (_, c) -> c.digitToInt() }
    )

    // SOLVE
    class DijkstraResultStorage {

        private val visited: MutableMap<Point, MutableSet<Origin>> = mutableMapOf()
        private val minHeatLosses: MutableMap<Point, MutableMap<Origin, Int>> = mutableMapOf()
        private val unvisitedQueue: PriorityQueue<Result> = PriorityQueue(compareBy { it.minHeatLoss })

        fun popUnvisitedResult(): Result = unvisitedQueue.poll()!!.also { (key) -> visitedAt(key.point) += key.origin }

        fun visitedAt(point: Point) = visited.computeIfAbsent(point) { mutableSetOf() }

        fun minHeatLossesAt(point: Point) = minHeatLosses.computeIfAbsent(point) { mutableMapOf() }

        fun isVisited(key: Key) = key.origin in visitedAt(key.point)

        fun register(result: Result) {
            val key = result.key
            val minHeatLosses = minHeatLossesAt(key.point)

            val oldLoss = minHeatLosses[key.origin]
            if (oldLoss == null || oldLoss > result.minHeatLoss) {
                minHeatLosses[key.origin] = result.minHeatLoss
                if (!isVisited(key)) fixUnvisitedQueue(result, oldLoss)
            }
        }

        private fun fixUnvisitedQueue(result: Result, oldLoss: Int?) {
            if (oldLoss != null) unvisitedQueue -= Result(result.key, oldLoss)
            unvisitedQueue += result
        }
    }

    fun startResult() = Result(Key(Point(0, 0), Origin(null, 0)), 0)

    fun GridRange.endpoint() = Point(xRange.last, yRange.last)

    fun Origin.next(nextDir: Dir) = Origin(nextDir, if (nextDir == dir) dirCount + 1 else 1)

    fun Key.next(nextDir: Dir) = Key(point.move(nextDir), origin.next(nextDir))

    fun City.minHeatLoss(canMove: (Dir, Origin) -> Boolean, canAccept: (Origin) -> Boolean): Int {
        val resultStorage = DijkstraResultStorage()
        resultStorage.register(startResult())

        val endpoint = gridRange.endpoint()
        fun canTerminate() = resultStorage.visitedAt(endpoint).any(canAccept)
        while (!canTerminate()) { // Dijkstra's algorithm (BFS + priority queue)
            val curResult = resultStorage.popUnvisitedResult()
            fun nextMinHeatLoss(key: Key) = curResult.minHeatLoss + heatLossMap[key.point]!!

            val curKey = curResult.key
            Dir.entries
                .filter { nextDir -> canMove(nextDir, curKey.origin) }
                .map { nextDir -> curKey.next(nextDir) }
                .filter { nextKey -> nextKey.point in gridRange && !resultStorage.isVisited(nextKey) }
                .map { nextKey -> Result(nextKey, nextMinHeatLoss(nextKey)) }
                .forEach(resultStorage::register)
        }

        return resultStorage.minHeatLossesAt(endpoint)
            .filterKeys(canAccept)
            .minOf { it.value }
    }

    fun part1(input: List<String>): Int {
        val city = input.parseCity()
        fun canMove(nextDir: Dir, origin: Origin) = when (origin.dir) {
            nextDir -> origin.dirCount < 3
            nextDir.opposite() -> false
            else -> true
        }
        return city.minHeatLoss(::canMove) { true }
    }

    fun part2(input: List<String>): Int {
        val city = input.parseCity()
        fun canAccept(origin: Origin) = origin.dirCount >= 4
        fun canMove(nextDir: Dir, origin: Origin) = when (origin.dir) {
            nextDir -> origin.dirCount < 10
            nextDir.opposite() -> false
            null -> true // first move
            else -> canAccept(origin)
        }
        return city.minHeatLoss(::canMove, ::canAccept)
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    102.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    94.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    1263.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
