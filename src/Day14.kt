fun main() {
    val day = "Day14"

    // MODEL
    data class Dish(val gridRange: GridRange, val cubeRocks: Set<Point>, val roundRocks: Set<Point>)

    data class DishBuilder(val gridRange: GridRange, val cubeRocks: Set<Point>, val roundRocks: MutableSet<Point>)

    // PARSE
    fun List<String>.toDish(): Dish = toPointMap().let { pointMap ->
        Dish(
            gridRange = toGridRange(),
            cubeRocks = pointMap.filterValues { it == '#' }.keys,
            roundRocks = pointMap.filterValues { it == 'O' }.keys
        )
    }

    // SOLVE
    fun DishBuilder.tiltPoints(points: List<Point>) {
        val barrierIndices = buildList {
            add(-1)
            addAll(points.indices.filter { i -> points[i] in cubeRocks })
            add(points.size)
        }
        val openSpaceIndexRanges = barrierIndices.zipWithNext { left, right -> left + 1..<right }

        for (indexRange in openSpaceIndexRanges) {
            val roundRockCount = indexRange.count { i -> roundRocks.remove(points[i]) }
            indexRange.take(roundRockCount).forEach { i -> roundRocks += points[i] }
        }
    }

    fun DishBuilder.tilt(pointLines: List<List<Point>>) = apply { pointLines.forEach { tiltPoints(it) } }

    fun DishBuilder.tiltNorth() = tilt(gridRange.pointColumns)

    fun DishBuilder.tiltSouth() = tilt(gridRange.pointColumns.map { it.asReversed() })

    fun DishBuilder.tiltWest() = tilt(gridRange.pointRows)

    fun DishBuilder.tiltEast() = tilt(gridRange.pointRows.map { it.asReversed() })

    fun Dish.builder() = DishBuilder(gridRange, cubeRocks, roundRocks.toMutableSet())

    fun DishBuilder.build() = Dish(gridRange, cubeRocks, roundRocks.toSet())

    fun Dish.tiltNorth() = builder().tiltNorth().build()

    fun Dish.spin() = builder().tiltNorth().tiltWest().tiltSouth().tiltEast().build()

    fun Dish.computeNorthLoad() = roundRocks.sumOf { (_, y) -> gridRange.yRange.size() - y }

    fun Dish.toNiceString() = gridRange.toNiceString { p ->
        when (p) {
            in cubeRocks -> '#'
            in roundRocks -> 'O'
            else -> '.'
        }
    }

    class SpinCycleFinder(
        private var dish: Dish,
        private val printDish: Boolean = false
    ) {

        fun dishAfterSpins(n: Int): Dish {
            val start = reachCycleStart()
            val cyclicDishes = detectCyclicDishes()
            val cycle = Cycle(start, cyclicDishes.size)
            println(cycle)

            return cyclicDishes[cycle.offset(n)]
        }

        private fun reachCycleStart() = buildSet {
            while (add(dish)) {
                dish = dish.spin()
                if (printDish && size <= 3) println("\nSpin $size:\n${dish.toNiceString()}")
            }
        }.size

        private fun detectCyclicDishes() = buildList {
            do {
                add(dish)
                dish = dish.spin()
            } while (dish != first())
        }
    }

    fun part1(input: List<String>, printDish: Boolean = false): Int {
        val dish = input.toDish()
        val tiltedDish = dish.tiltNorth()
        if (printDish) println(tiltedDish.toNiceString())
        return tiltedDish.computeNorthLoad()
    }

    fun part2(input: List<String>, printDish: Boolean = false): Int {
        val dish = input.toDish()
        return SpinCycleFinder(dish, printDish)
            .dishAfterSpins(n = 1000000000)
            .computeNorthLoad()
    }

    // TESTS
    val test1 = part1(readInput("$day/test"), printDish = true)
    136.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"), printDish = true)
    64.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    113078.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
