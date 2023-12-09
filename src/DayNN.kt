fun main() {
    val day = "DayNN"

    // MODEL


    // PARSE


    // SOLVE


    fun part1(input: List<String>): Long {
        return 0
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 0L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 0L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
