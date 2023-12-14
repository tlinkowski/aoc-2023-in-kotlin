fun main() {
    val day = "Day14"

    // MODEL
//    data class Point(val x: Int, val y: Int)
//
//    data class Rock

    // PARSE


    // SOLVE
    fun List<Char>.tilt(): String {
        val barrierIndexes = (listOf(-1) + mapIndexedNotNull { i, c -> if (c == '#') i else null } + listOf(size))
            .toSet()
        val roundRockIndexes = barrierIndexes.zipWithNext { a, b ->
            val count = (a + 1..<b).count { i -> this[i] == 'O' }
            (a + 1..<a + 1 + count)
        }
            .flatten()
            .toSet()

        return indices.reversed().map { i ->
            when (i) {
                in barrierIndexes -> '#'
                in roundRockIndexes -> 'O'
                else -> '.'
            }
        }.joinToString("")
    }

    fun List<String>.tilt() = this[0].indices.map { x -> map { it[x] }.tilt() }

    fun List<String>.tiltFullCycle() = tilt().tilt().tilt().tilt()

    fun String.load() = mapIndexed { i, c -> if (c == 'O') i + 1 else 0 }.sum().toLong()

    fun List<String>.load() = sumOf { it.load() }

    fun List<String>.rotate() = this[0].indices.map { x -> map { it[x] }.reversed().joinToString("") }

    fun part1(input: List<String>): Long {
        return input.tilt().load()
    }

    fun part2(input: List<String>): Long {
        val period = 8
        val results = generateSequence { input }.take(period).toList().toTypedArray()
        for (i in 1..1000) {
            val nextResult = results[(i - 1) % period].tiltFullCycle()
            println(nextResult.rotate().load())
            if (results[i % period] == nextResult) {
                break
            } else {
                results[i % period] = nextResult
            }
        }
//        results.forEach { println(it.load()) }
//        val result = generateSequence(input) { it.tilt() }.take(1000000000)
//        input.tilt().onEach(::println)
        return results[1000000000 % period].rotate().load()
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    check(test1 == 136L) { "Test 1: $test1 (wrong)" }

    val test2 = part2(readInput("$day/test"))
    check(test2 == 64L) { "Test 2: $test2 (wrong)" }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    check(part1 == 113078L) { "Part 1: $part1 (wrong)" }
    println("Part 1: $part1")
    println("Part 2: ${part2(input)}")
}
