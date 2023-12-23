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
        println("Stats: neighbors.size=${neighbors.size}, lengths.size=${lengths.size}, branches=${neighbors.values.sumOf {it.size} / 2}")
        return Graph(start, end, neighbors, lengths)
    }

    data class GHike(val cur: Point, val visited: Set<Point>, val length: Int)

    fun Graph.nextSteps(hike: GHike): List<GHike> {
        val p = hike.cur
        check(p != end)
        return neighbors[p]!!
            .filter { it !in hike.visited }
            .map { GHike(it, hike.visited + it, hike.length + lengths[setOf(p, it)]!!)}
    }

    fun Forest.toNiceString(graph: Graph): String {
        return gridRange.toNiceString { p ->
            when (p) {
                in trees -> '#'
                in graph.neighbors -> graph.neighbors[p]!!.size.toString().last()
//                in graph.neighbors -> {
//                    val neighbor = graph.neighbors[p]!!.first()
//                    graph.lengths[setOf(p, neighbor)]!!.toString().last()
//                }
                else -> '.'
            }
        }
        // Dir.entries
        //                    .map { p.move(it) }
        //                    .mapNotNull { graph.neighbors[it]?. }
        //
        //
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
        val endHikes = mutableListOf<GHike>()

        var hikesInProgress = mapOf(start to listOf(startHike))

        while (hikesInProgress.isNotEmpty()) {
//            if (hikes.size < 10) println(hikes)
            println("End: ${endHikes.size}, in progress: ${hikesInProgress.values.sumOf { it.size}}")
            val nextHikesInProgress = mutableMapOf<Point, MutableList<GHike>>()
            for (hh in hikesInProgress.values) {
                for (h in hh) {
                    nextSteps(h).forEach { nh ->
                        if (nh.cur == end) endHikes += nh else nextHikesInProgress.at(nh.cur) += nh
                    }
                }
            }
            hikesInProgress = nextHikesInProgress
                .mapValues { (_, v) ->
                    val maxLength = v.maxOf { it.length }
                    v.filter { it.length >= maxLength - 500 }
                }
        }

        val longest = endHikes.maxBy { it.length }
//        println(toNiceString(longest))
        return longest
    }

    fun Graph.longestHike(): GHike {
        val startHike = GHike(start, setOf(start), 0)
        val endHikes = mutableListOf<GHike>()

        var hikesInProgress = listOf(startHike)
        while (hikesInProgress.isNotEmpty()) {
//            if (hikes.size < 10) println(hikes)
            println("End: ${endHikes.size}, in progress: ${hikesInProgress.size}")
            val nextHikesInProgress = ArrayList<GHike>(hikesInProgress.size * 2)
            for (h in hikesInProgress) {
                nextSteps(h).forEach { nh ->
                    if (nh.cur == end) endHikes += nh else nextHikesInProgress += nh
                }
            }
            hikesInProgress = nextHikesInProgress

//            val result = hikesInProgress.flatMap { nextSteps(it) }
//            hikesInProgress = result.filter { it.cur != end }
//            endHikes += result.filter { it.cur == end }
        }

        val longest = endHikes.maxBy { it.length }
//        println(toNiceString(longest))
        return longest
    }

    fun part2(input: List<String>): Int {
        val forest = input.parseForest()
        val graph = forest.toGraph()
        println()
        println(forest.toNiceString(graph))
        println()
        val longestHike = graph.longestHikeMap()

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
    println("Part 2: $part2") // 4794 too low, 5654 too low
}
