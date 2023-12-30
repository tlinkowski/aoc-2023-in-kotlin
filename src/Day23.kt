import kotlin.math.max

fun main() {
    val day = "Day23"

    // MODEL
    data class ForestMap(
        val gridRange: GridRange, val start: Point, val end: Point,
        val trees: Set<Point>, val slopes: Map<Point, Dir>, val junctions: Set<Point>
    )

    data class Position(val sourceDir: Dir, val point: Point)

    val pointComparator = compareBy<Point> { it.x }.thenBy { it.y }

    data class Edge(val a: Point, val b: Point) {
        init {
            check(pointComparator.compare(a, b) < 0)
        }
    }

    data class ForestGraph(
        val start: Point, val end: Point,
        val neighbors: Map<Point, List<Point>>,
        val paths: Map<Edge, List<Point>>
    )

    data class Hike(val currentNode: Point, val visitedNodes: Set<Point>, val distance: Int)

    // PARSE
    fun List<String>.parseForestMap(): ForestMap {
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

        fun isPassable(point: Point) = point in gridRange && point !in trees
        fun neighborCount(point: Point) = Dir.entries.map(point::move).count(::isPassable)
        val junctions = pointMap.filterValues { it == '.' }.keys
            .filter { point -> neighborCount(point) > 2 }
            .toSet()

        return ForestMap(gridRange, start, end, trees, slopes, junctions)
    }

    // SOLVE
    fun ForestMap.isPassable(point: Point) = point in gridRange && point !in trees

    fun ForestMap.isGraphNode(point: Point) = point == start || point == end || point in junctions

    fun ForestMap.initialDirections(point: Point) = if (point in slopes) listOf(slopes[point]!!) else Dir.entries

    fun Point.toPositionAlong(dir: Dir) = Position(dir, move(dir))

    fun Position.tryMoveAlong(dir: Dir) = if (dir != sourceDir.opposite()) point.toPositionAlong(dir) else null

    fun ForestMap.findPathAlongDirection(source: Point, sourceDir: Dir): List<Point>? {
        check(isGraphNode(source))
        var position = source.toPositionAlong(sourceDir)
        val path = mutableListOf(source, position.point)

        while (!isGraphNode(position.point)) {
            position = initialDirections(position.point)
                .mapNotNull { dir -> position.tryMoveAlong(dir) }
                .singleOrNull { isPassable(it.point) }
                ?: return null
            path += position.point
        }

        return path
    }

    fun edge(a: Point, b: Point) = if (pointComparator.compare(a, b) < 0) Edge(a, b) else Edge(b, a)

    fun ForestMap.toGraph(directed: Boolean): ForestGraph {
        val neighbors = mutableMapOf<Point, MutableList<Point>>()
        val paths = mutableMapOf<Edge, List<Point>>()

        val nodes = junctions + start + end
        for (node in nodes) {
            Dir.entries
                .filter { dir -> isPassable(node.move(dir)) }
                .mapNotNull { dir -> findPathAlongDirection(node, dir) }
                .forEach { path ->
                    val source = path.first()
                    val target = path.last()
                    neighbors.at(source) += target
                    if (!directed) neighbors.at(target) += source
                    paths[edge(source, target)] = path
                }
        }

        println("Nodes: ${nodes.size}, directed edges: ${neighbors.values.sumOf { it.size } / 2}, total distance: ${paths.values.sumOf { it.size - 1 }}")
        return ForestGraph(start, end, neighbors, paths)
    }

    fun ForestGraph.neighbors(point: Point) = neighbors[point]!!

    fun ForestGraph.pathBetween(a: Point, b: Point) = paths[edge(a, b)]!!

    fun ForestGraph.distanceBetween(a: Point, b: Point): Int = pathBetween(a, b).size - 1

    fun ForestGraph.tryHikeTo(hike: Hike, next: Point): Hike? = if (next !in hike.visitedNodes)
        Hike(next, hike.visitedNodes + next, hike.distance + distanceBetween(hike.currentNode, next))
    else null

    fun ForestGraph.tryHikeToNeighbors(hike: Hike): List<Hike> = neighbors(hike.currentNode)
        .mapNotNull { neighbor -> tryHikeTo(hike, neighbor) }

    val bestDistanceRatio = 0.90

    fun ForestGraph.longestHike(): Hike {
        val seed = Hike(start, setOf(start), 0)
        var inProgress = mapOf(start to listOf(seed))
        var result = seed
        var maxHikesInProgress = 0 // debug info

        while (inProgress.isNotEmpty()) { // BFS
            maxHikesInProgress = max(maxHikesInProgress, inProgress.values.sumOf { it.size })

            val nextStepHikes = inProgress.values.asSequence()
                .flatMap { hikesToPoint ->
                    // keep only best hikes to given point
                    val minDistance = bestDistanceRatio * hikesToPoint.maxOf { it.distance }
                    hikesToPoint.asSequence().filter { it.distance >= minDistance }
                }
                .flatMap { hike -> tryHikeToNeighbors(hike) }

            inProgress = nextStepHikes.filter { it.currentNode != end }.groupBy { it.currentNode }
            result = nextStepHikes.filter { it.currentNode == end }.plus(result).maxBy { it.distance }
        }
        println("Max hikes in progress during BFS: $maxHikesInProgress")

        return result
    }

    fun Hike.allVisitedPoints(forestGraph: ForestGraph) =
        visitedNodes.zipWithNext(forestGraph::pathBetween).flatten().toSet()

    fun ForestMap.toNiceString(visitedPoints: Set<Point>) = gridRange.toNiceString { p ->
        when (p) {
            in visitedPoints -> 'O'
            start -> 'S'
            end -> 'E'
            in trees -> '#'
            in slopes -> slopes[p]!!.symbol
            in junctions -> '+'
            else -> '.'
        }
    }

    fun Hike.toNiceString(forestMap: ForestMap, forestGraph: ForestGraph) =
        forestMap.toNiceString(allVisitedPoints(forestGraph))

    fun part(input: List<String>, directed: Boolean, print: Boolean): Int {
        val forestMap = input.parseForestMap()
        val forestGraph = forestMap.toGraph(directed)
        val longestHike = forestGraph.longestHike()
        if (print) println(longestHike.toNiceString(forestMap, forestGraph))

        return longestHike.distance
    }

    fun part1(input: List<String>, print: Boolean = false): Int = part(input, directed = true, print)

    fun part2(input: List<String>, print: Boolean = false): Int = part(input, directed = false, print)

    // TESTS
    val test1 = part1(readInput("$day/test"), print = true)
    94.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"), print = true)
    154.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    2086.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
