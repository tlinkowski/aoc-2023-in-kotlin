fun main() {
    val day = "Day14"

    // MODEL
//    data class Point(val x: Int, val y: Int)
//
//    data class Rock

    // PARSE


    // SOLVE
    fun List<Char>.solve(): Long {
        val barrierIndexes = listOf(-1) + mapIndexedNotNull { i, c -> if (c == '#') i else null } + listOf(size)
        val roundRockIndexes = barrierIndexes.zipWithNext { a, b ->
            val count = (a+1..<b).count { i -> this[i] == 'O' }
            (a+1..<a+1+count)
        }
            .flatten()
        return roundRockIndexes.sumOf { size - it }.toLong()
    }

    fun part1(input: List<String>): Long {
        return input[0].indices.sumOf { x ->
            input.map { it[x] }.solve().also(::println)
        }
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    check(test1 == 136L) { "Test 1: $test1 (wrong)" }

    val test2 = part2(readInput("$day/test"))
    check(test2 == 0L) { "Test 2: $test2 (wrong)" }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    check(part1 == 113078L) { "Part 1: $part1 (wrong)" }
    println("Part 1: $part1")
    println("Part 2: ${part2(input)}")
}
