fun main() {
    val day = "Day21"

    // MODEL
    // remaining 64 steps
// He gives you an up-to-date map (your puzzle input) of his
// starting position (S), garden plots (.), and rocks (#)
    data class Garden(val gridRange: GridRange, val start: Point, val rocks: Set<Point>)

    // PARSE
    fun List<String>.parseGarden(): Garden {
        val pointMap = toPointMap()
        val start = pointMap.firstNotNullOf { (p, c) -> if (c == 'S') p else null }
        val rocks = pointMap.filterValues { it == '#' }.keys
        return Garden(toGridRange(), start, rocks)
    }

    fun GridRange.adapted(p: Point) = Point(
        (p.x % xRange.size() + xRange.size()) % xRange.size(),
        (p.y % yRange.size() + yRange.size()) % yRange.size()
    )

    fun GridRange.expanded(times: Int) = GridRange(
        xRange = xRange.first - times * xRange.size()..xRange.last + times * xRange.size(),
        yRange = yRange.first - times * yRange.size()..yRange.last + times * yRange.size(),
    )

    // SOLVE
    fun GridRange.move(xTimes: Int, yTimes: Int) = GridRange(
        xRange = (xRange.size() * xTimes).let { xRange.first + it..xRange.last + it },
        yRange = (yRange.size() * yTimes).let { yRange.first + it..yRange.last + it }
    )

    data class Uni(
        val base: GridRange,
        val times: Int = 0,
        val spanning: GridRange = base,
        val all: Set<GridRange> = setOf(base)
    ) {
        fun expand(): Uni {
            val newTimes = times + 1
            return Uni(
                base = base,
                times = newTimes,
                spanning = base.expanded(newTimes),
                all = (-newTimes..newTimes).flatMap { xTimes ->
                    (-newTimes..newTimes).map { yTimes -> base.move(xTimes, yTimes) }
                }.toSet()
            )
        }
    }

    fun Garden.reachablePlots(steps: Int): Int {
        val stepCycles = mutableListOf<Int>()

        var reachablePoints = setOf(start)
        var uni = Uni(gridRange)
        for (step in 1..2000) {

            val nextReachablePoints = reachablePoints
                .flatMap { p ->
                    Dir.entries
                        .map { d -> p.move(d) }
                        .filter { pp -> gridRange.adapted(pp) !in rocks }
                }
                .toSet()

            if (nextReachablePoints.any { it !in uni.spanning }) {
                val prevStep = step - 1
                stepCycles += prevStep
                val stepCycle = stepCycles.last() - stepCycles.getOrElse(stepCycles.size - 2) { 0 }
                val uniCountsDescription = uni.all.map { gr -> reachablePoints.count { it in gr } }
                    .filter { it > 0 }
                    .groupBy { it }
                    .entries
                    .sortedBy { it.key }
                    .joinToString(" + ") { "${it.key}*${it.value.size}" }
                println("x${stepCycles.size - 1}------ step $prevStep (cycle: $stepCycle) -> grids: ${uni.all.size}, ${reachablePoints.size} = $uniCountsDescription")

                uni = uni.expand()
            }

            reachablePoints = nextReachablePoints

//            println("\nStep $step")
//            println(gridRange.toNiceString {
//                when (gridRange.adapted(it)) {
//                    in reachablePoints -> 'O'
//                    in rocks -> '#'
//                    else -> '.'
//                }
//            })
        }

        return reachablePoints.size
    }

    fun part1(input: List<String>, steps: Int): Int {
        val garden = input.parseGarden()

        return garden.reachablePlots(steps)
    }

    fun part2(input: List<String>, steps: Int): Long {
        val garden = input.parseGarden()

        return garden.reachablePlots(steps).toLong()
    }

    // TESTS
//    val testInput = readInput("$day/test")
//    val test1 = part1(testInput, steps = 6)
//    16.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }
//
//    val test2 = part2(testInput, steps = 1000)
//    50L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }
//
//    val test2a = part2(testInput, steps = 50)
//    1594L.let { check(test2a == it) { "Test 2a: is $test2a, should be $it" } }
//
//    val test2b = part2(testInput, steps = 100)
//    6536L.let { check(test2a == it) { "Test 2b: is $test2b, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")

    //    val part1 = part1(input, 64)
//    3617.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input, 26501365)
    println("Part 2: $part2")
}
