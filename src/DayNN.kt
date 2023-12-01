fun main() {
    val day = "DayNN"

    fun part1(input: List<String>): Int {
        return 0
    }

    fun part2(input: List<String>): Int {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test1"))
    check(part1 == 0) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 0) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
