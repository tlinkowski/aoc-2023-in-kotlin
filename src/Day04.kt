import kotlin.math.pow

typealias CardId = Int

fun main() {
    val day = "Day04"

    data class Card(
        val id: CardId,
        val winningNumbers: Set<Int>,
        val myNumbers: Set<Int>
    ) {
        fun wonCardCount() = (winningNumbers intersect myNumbers).size

        fun points() = wonCardCount()
            .let { count -> if (count == 0) 0 else 2.0.pow(count - 1).toInt() }

        fun wonCardIds() = (id + 1)..(id + wonCardCount())
    }

    fun String.parseNumbers() = split(' ')
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
        val cards = input.map { parseCard(it) }

        val cardCounts = buildMap<CardId, Int> {
            cards.forEach { card ->
                this[card.id] = 1
            }
            cards.forEach { card ->
                val cardCount = this[card.id]!!
                card.wonCardIds().forEach { id -> merge(id, cardCount, Int::plus) }
            }
        }

        return cardCounts.values.sum()
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
