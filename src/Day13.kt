interface PlaneView {
    val xMax: Int
    val yMax: Int

    fun symbol(x: Int, y: Int): Char
}

fun main() {
    val day = "Day13"

    // MODEL
    class Plane(private val lines: List<String>) : PlaneView {
        override val xMax
            get() = lines[0].length - 1
        override val yMax
            get() = lines.size - 1

        override fun symbol(x: Int, y: Int) = lines[y][x]
    }

    class InvertedPlane(private val plane: Plane) : PlaneView {
        override val xMax
            get() = plane.yMax
        override val yMax
            get() = plane.xMax

        override fun symbol(x: Int, y: Int) = plane.symbol(y, x)
    }

    // PARSE
    fun List<String>.parsePlanes(): List<Plane> = joinToString("\n").split("\n\n")
        .filter { it.isNotBlank() }
        .map { Plane(it.trim().lines()) }

    // SOLVE
    fun PlaneView.isMirrorLine(xRef: Int, expectedSmudges: Int): Boolean {
        var actualSmudges = 0

        val xRange = if (xRef <= xMax / 2) 0..<xRef else xRef..xMax
        for (x in xRange) {
            val xMirrored = 2 * xRef - x - 1
            for (y in 0..yMax) {
                if (symbol(x, y) != symbol(xMirrored, y) && ++actualSmudges > expectedSmudges) {
                    return false
                }
            }
        }

        return actualSmudges == expectedSmudges
    }

    fun PlaneView.mirrorIndexX(smudges: Int): Int? = (1..xMax).firstOrNull { x -> isMirrorLine(x, smudges) }

    fun Plane.mirrorIndexY(smudges: Int): Int = InvertedPlane(this).mirrorIndexX(smudges)!!

    fun Plane.reflectionLineNumer(smudges: Int): Int = mirrorIndexX(smudges) ?: (100 * mirrorIndexY(smudges))

    fun part1(input: List<String>): Long {
        val planes = input.parsePlanes()
        return planes.sumOf { it.reflectionLineNumer(smudges = 0).toLong() }
    }

    fun part2(input: List<String>): Long {
        val planes = input.parsePlanes()
        return planes.sumOf { it.reflectionLineNumer(smudges = 1).toLong() }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    check(test1 == 405L) { "Test 1: $test1 (wrong)" }

    val test2 = part2(readInput("$day/test"))
    check(test2 == 400L) { "Test 2: $test2 (wrong)" }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    check(part1 == 33728L) { "Part 1: $part1 (wrong)" }
    println("Part 1: $part1")
    println("Part 2: ${part2(input)}")
}
