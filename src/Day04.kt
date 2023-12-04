import kotlin.math.pow

fun main() {
    val day = "Day04"

    data class Card(
        val id: Int,
        val winningNumbers: Set<Int>,
        val myNumbers: Set<Int>
    ) {
        fun wonCardCount() = winningNumbers.intersect(myNumbers).size

        fun points() = wonCardCount()
            .takeIf { it > 0 }
            ?.let { 2.0.pow(it - 1).toInt() }
            ?: 0
    }

    fun String.parseNumbers() = trim().split(' ')
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }
        .toSet()

    fun parseCard(line: String) = Card(
        id = line.removePrefix("Card").substringBefore(':').trim().toInt(),
        winningNumbers = line.substringAfter(':')
            .substringBefore('|')
            .parseNumbers(),
        myNumbers = line.substringAfter('|')
            .parseNumbers(),
    )

    fun part1(input: List<String>): Int {
        return input
            .map { parseCard(it) }
            .sumOf { it.points() }
    }

    fun part2(input: List<String>): Int {
        val cardMap = input
            .map { parseCard(it) }
            .associateBy { it.id }

        fun Card.withWonCards(): List<Card> = listOf(this) + (id + 1..id + wonCardCount())
            .mapNotNull { cardMap[it] }
            .flatMap { it.withWonCards() }

        val myCards = cardMap.values.flatMap { it.withWonCards() }
        return myCards.size
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 13) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 30) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
