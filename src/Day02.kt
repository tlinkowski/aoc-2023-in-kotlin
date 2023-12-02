import kotlin.math.max

enum class Color { RED, GREEN, BLUE }

fun main() {
    val day = "Day02"

    data class Draw(
        val cubes: Map<Color, Int>
    ) {
        fun isPossible(bag: Map<Color, Int>) = cubes.all { (color, count) ->
            bag.getOrDefault(color, 0) >= count
        }

        fun minimumBag(): Map<Color, Int> = Color.entries.associateWith { color ->
            cubes.getOrDefault(color, 0)
        }
    }

    fun String.parseDraw() = Draw(
        cubes = split(", ")
            .associate { Color.valueOf(it.substringAfter(' ').uppercase()) to it.substringBefore(' ').toInt() }
    )

    data class Game(
        val id: Int,
        val draws: List<Draw>
    ) {
        fun isPossible(bag: Map<Color, Int>) = draws.all { it.isPossible(bag) }

        fun minimumBag(): Map<Color, Int> = draws
            .map { it.minimumBag() }
            .reduce { bag1, bag2 ->
                Color.entries.associateWith { color ->
                    max(bag1[color]!!, bag2[color]!!)
                }
            }
    }

    fun String.parseGame() = Game(
        id = substringBefore(':').removePrefix("Game ").toInt(),
        draws = substringAfter(": ")
            .split("; ")
            .map { it.parseDraw() }
    )

    fun part1(input: List<String>): Int {
        val bag = mapOf(
            Color.RED to 12,
            Color.GREEN to 13,
            Color.BLUE to 14
        )

        return input
            .map { it.parseGame() }
            .filter { it.isPossible(bag) }
            .sumOf { it.id }
    }

    fun Map<Color, Int>.power() = values
        .fold(1) { power, count -> power * count }

    fun part2(input: List<String>): Int {
        return input
            .map { it.parseGame() }
            .map { it.minimumBag() }
            .sumOf { it.power() }
    }

    // TESTS
    val part1 = part1(readInput("$day/test1"))
    check(part1 == 8) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 2286) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
