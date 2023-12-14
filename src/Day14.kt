fun main() {
    val day = "Day14"

    // MODEL
    data class Dish(val lines: List<String>) {
        val xRange = lines[0].indices
        val yRange = lines.indices

        override fun toString() = lines.joinToString("\n") + "\n"
    }

    // PARSE
    fun List<String>.toDish() = Dish(this)

    // SOLVE
    val cubeRock = '#'
    val roundRock = 'O'

    fun List<Char>.tilt(): List<Char> {
        fun IntRange.countRoundRocks() = count { i -> get(i) == roundRock }
        val cubeRocks = indices.filter { i -> get(i) == cubeRock }.toSet()
        val barriers = listOf(-1) + cubeRocks + size
        val roundRocks = barriers
            .zipWithNext { left, right -> left + 1..<right }
            .flatMap { range -> range.take(range.countRoundRocks()) }
            .toSet()

        fun symbol(i: Int) = when (i) {
            in cubeRocks -> cubeRock
            in roundRocks -> roundRock
            else -> '.'
        }
        return indices.map { i -> symbol(i) }
    }

    fun List<Char>.reverseTilt() = reversed().tilt().reversed()

    fun Dish.mapVertically(f: (List<Char>) -> List<Char>): Dish {
        val mappedXY = xRange.map { x -> f(lines.map { it[x] }) }
        return yRange.map { y ->
            xRange.map { x -> mappedXY[x][y] }.joinToString("")
        }.let { Dish(it) }
    }

    fun Dish.mapHorizontally(f: (List<Char>) -> List<Char>): Dish = Dish(
        lines.map { line -> f(line.toList()).joinToString("") }
    )

    fun Dish.tiltNorth(): Dish = mapVertically { it.tilt() }

    fun Dish.tiltSouth(): Dish = mapVertically { it.reverseTilt() }

    fun Dish.tiltWest(): Dish = mapHorizontally { it.tilt() }

    fun Dish.tiltEast(): Dish = mapHorizontally { it.reverseTilt() }

    fun Dish.fullTilt() = tiltNorth().tiltWest().tiltSouth().tiltEast()

    fun Dish.computeNorthLoad() = lines.mapIndexed { y, line ->
        (lines.size - y) * line.count { it == roundRock }
    }.sum()

    class TiltCycleFinder(private var dish: Dish, private val printDish: Boolean = false) {
        var tiltCount = 0

        fun dishAfterFullTilts(n: Int): Dish {
            reachCycle()
            println("Cycle formed after $tiltCount full tilts ")

            val cycle = detectCycle()
            println("Cycle length is " + cycle.size)

            return cycle[(n - tiltCount) % cycle.size]
        }

        private fun reachCycle() = buildSet {
            while (add(dish)) {
                tiltDish()
                if (printDish && tiltCount <= 3) println("Full tilt $tiltCount:\n$dish")
            }
        }

        private fun detectCycle() = buildList {
            do {
                add(dish)
                tiltDish()
            } while (dish != first())
        }

        private fun tiltDish() {
            dish = dish.fullTilt()
            tiltCount++
        }
    }

    fun part1(input: List<String>, printDish: Boolean = false): Int {
        val dish = input.toDish()
        val tiltedDish = dish.tiltNorth()
        if (printDish) println(tiltedDish)
        return tiltedDish.computeNorthLoad()
    }

    fun part2(input: List<String>, printDish: Boolean = false): Int {
        val dish = input.toDish()
        return TiltCycleFinder(dish, printDish)
            .dishAfterFullTilts(n = 1000000000)
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
