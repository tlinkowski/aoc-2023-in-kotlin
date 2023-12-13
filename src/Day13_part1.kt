fun main() {
    val day = "Day13"

    // MODEL
    data class Plane(
        val hPlane: List<String>,
        val vPlane: List<String>
    ) {
        override fun toString() = hPlane.joinToString("\n") +
        "\n\n" + vPlane.joinToString("\n") + "\n"
    }

    // PARSE
    fun List<String>.invert() = this[0].indices.map { x ->
        joinToString("") { line -> line[x].toString() }
    }

    fun List<String>.parsePlanes(): List<Plane> = joinToString("\n")
        .split("\n\n")
        .filter { it.isNotBlank() }
        .map { it.trim().lines() }
        .map { Plane(it, it.invert() )}

    // SOLVE
    fun List<String>.reflectionIndex(): Int? {
        return (1..<size).firstOrNull { left ->
            val right = size - left
            if (left <= right) {
                (0..<left).all { i ->
                    val diff = left - i - 1
                    val iReflect = i + 2 * diff + 1
                    println("1 left=$left: i=$i -> iR=$iReflect")
                    this[i] == this[iReflect]
                }
            } else {
                (left..<size).all { i ->
                    val diff = i - left
                    val iReflect = i - 2 * diff - 1
                    println("2 left=$left: i=$i -> iR=$iReflect")
                    this[i] == this[iReflect]
                }
            }
        }
    }

    fun Plane.reflectionNumber(): Int {
        return vPlane.reflectionIndex()
            ?: (100 * hPlane.reflectionIndex()!!)
    }

    fun part1(input: List<String>): Long {
        val planes = input.parsePlanes().onEach(::println)
        return planes.sumOf { it.reflectionNumber().toLong() }
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    check(test1 == 405L) { "Test 1: $test1 (wrong)" }

    val test2 = part2(readInput("$day/test"))
    check(test2 == 0L) { "Test 2: $test2 (wrong)" }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    check(part1 == 33728L) { "Part 1: $part1 (wrong)" }
    println("Part 1: $part1")
    println("Part 2: ${part2(input)}")
}
