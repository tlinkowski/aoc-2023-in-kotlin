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
    val test1 = part1(readInput("$day/test"))
    check(test1 == 0L) { "Test 1: $test1 (wrong)" }

    val test2 = part2(readInput("$day/test"))
    check(test2 == 0L) { "Test 2: $test2 (wrong)" }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    check(part1 == 0L) { "Part 1: $part1 (wrong)" }
    println("Part 1: $part1")
    println("Part 2: ${part2(input)}")
}
