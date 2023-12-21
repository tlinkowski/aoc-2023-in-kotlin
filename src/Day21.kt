fun main() {
    val day = "Day21"

    // MODEL
    data class Garden(val gridRange: GridRange, val start: Point, val rocks: Set<Point>)

    data class Scope(val base: GridRange, val times: Int = 0) {
        val spanning = base.expand(times)
    }

    // PARSE
    fun List<String>.parseGarden(): Garden {
        val pointMap = toPointMap()
        val start = pointMap.firstNotNullOf { (p, c) -> if (c == 'S') p else null }
        val rocks = pointMap.filterValues { it == '#' }.keys
        return Garden(toGridRange(), start, rocks)
    }

    // SOLVE
    fun Garden.toNiceString(reachablePoints: Set<Point>) = gridRange.toNiceString { point ->
        when (point) {
            in reachablePoints -> 'O'
            in rocks -> '#'
            else -> '.'
        }
    }

    fun Garden.nextPointsDirect(reachablePoints: Set<Point>) = reachablePoints.flatMap { point ->
        Dir.entries
            .map { dir -> point.move(dir) }
            .filter { it !in rocks }
    }.toSet()

    fun Garden.nextPointsNormalized(reachablePoints: Set<Point>) = reachablePoints.flatMap { point ->
        Dir.entries
            .map { dir -> point.move(dir) }
            .filter { gridRange.normalize(it) !in rocks }
    }.toSet()

    fun Scope.expand() = copy(times = times + 1)

    operator fun Scope.contains(points: Collection<Point>) = points.all { it in spanning }

    fun Garden.reachablePlotsDirect(stepCount: Int, print: Boolean): Int {
        var reachablePoints = setOf(start)
        for (step in 1..stepCount) {
            reachablePoints = nextPointsDirect(reachablePoints)

            if (print && step <= 3) {
                println("\nStep $step")
                println(toNiceString(reachablePoints))
            }
        }

        return reachablePoints.size
    }

    fun Garden.reachablePlotsExtrapolated(stepCount: Int, print: Boolean): Long {
        if (print) println("Looking for step $stepCount")

        val pointCountsPerStep = mutableListOf(1)
        val stepsOnScopeBorder = mutableListOf<Int>()

        var reachablePoints = setOf(start)
        var scope = Scope(gridRange)
        var step = 0
        val minStepForExtrapolation = 50

        fun findExtrapolationSteps(cycle: Cycle) = cycle.progression(stepCount).asSequence()
            .dropWhile { it < minStepForExtrapolation }.take(3).toList()

        var cycle: Cycle? = null
        var extrapolationSteps = listOf<Int>()

        while (cycle == null || extrapolationSteps.last() > step) {
            step++
            reachablePoints = nextPointsNormalized(reachablePoints)
            pointCountsPerStep += reachablePoints.size

            if (step == stepCount) {
                return reachablePoints.size.toLong()
                    .also { if (print) println("- direct return: $it") }
            }

            if (reachablePoints !in scope) {
                stepsOnScopeBorder += step - 1 // border crossed at `step` => border edge reached at `step-1`
                if (print && step > 100) println(" - reached border at step ${step - 1}")

                if (step >= minStepForExtrapolation && cycle == null) {
                    cycle = stepsOnScopeBorder.findCycle()
                    if (cycle != null) {
                        if (print) println("- found $cycle")
                        extrapolationSteps = findExtrapolationSteps(cycle)
                    }
                }

                scope = scope.expand()
            }
        }

        val extrapolationPoints = extrapolationSteps.map { s -> Point(x = s, y = pointCountsPerStep[s]) }
        val reachablePlotCountFunction = lagrange(extrapolationPoints)
        return reachablePlotCountFunction(stepCount)
            .also { if (print) println("- extrapolated return: $it (used $extrapolationPoints)") }
    }

    fun part1(input: List<String>, steps: Int, print: Boolean = false): Int {
        val garden = input.parseGarden()

        return garden.reachablePlotsDirect(steps, print)
    }

    fun part2(input: List<String>, steps: Int, print: Boolean = false): Long {
        val garden = input.parseGarden()

        return garden.reachablePlotsExtrapolated(steps, print)
    }

    // TESTS
    val testInput = readInput("$day/test")
    val test1 = part1(testInput, steps = 6, print = true)
    16.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    for ((steps, result) in mapOf(
        6 to 16L,
        10 to 50L,
        50 to 1594L,
        100 to 6536L,
        500 to 167004L,
        1000 to 668697L,
        5000 to 16733044L
    )) {
        val test2 = part2(testInput, steps = steps, print = true)
        check(test2 == result) { "Test 2 ($steps steps): is $test2, should be $result" }
    }

    // RESULTS
    val input = readInput("$day/input")

    val part1 = part1(input, steps = 64)
    3617.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input, steps = 26501365, print = true)
    println("Part 2: $part2")
}
