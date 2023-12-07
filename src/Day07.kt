enum class CardType(private val n1: Int, private val n2: Int) {
    FIVE(5, 0),
    FOUR(4, 1),
    FULL(3, 2),
    THREE(3, 1),
    TWO_PAIR(2, 2),
    ONE_PAIR(2, 1),
    HIGH_CARD(1, 1);

    fun isType(cardMap: Map<Char, Int>) = cardMap.values.toList().matches()

    fun isTypeWithJokers(cardMap: Map<Char, Int>) = cardMap.getOrDefault('J', 0).let { jokerCount ->
        jokerCount >= 4 || cardMap.filterKeys { it != 'J' }.values.toList().matches(jokerCount)
    }

    private fun List<Int>.matches(jokerCount: Int = 0) = this[0] + jokerCount == n1 && (n2 == 0 || this[1] == n2)
}

fun main() {

    val day = "Day07"

    fun String.toSortedCharMap() = toCharArray()
        .groupBy { it }
        .map { (card, cards) -> card to cards.size }
        .sortedByDescending { (_, size) -> size }
        .toMap()

    fun detectType(cards: String) = cards.toSortedCharMap().let { cardMap ->
        CardType.entries.first { it.isType(cardMap) }
    }

    fun detectTypeWithJokers(cards: String) = cards.toSortedCharMap().let { cardMap ->
        CardType.entries.first { it.isTypeWithJokers(cardMap) }
    }

    data class Hand(
        val cards: String,
        val type: CardType,
        val bid: Long
    ) {
        fun result(rank: Int) = bid * rank
    }

    fun handComparator(allCards: List<Char>) = compareBy<Hand> { it.type }
        .thenBy { allCards.indexOf(it.cards[0]) }
        .thenBy { allCards.indexOf(it.cards[1]) }
        .thenBy { allCards.indexOf(it.cards[2]) }
        .thenBy { allCards.indexOf(it.cards[3]) }
        .thenBy { allCards.indexOf(it.cards[4]) }
        .reversed()

    fun List<Hand>.totalResult() = mapIndexed { index, hand -> hand.result(rank = index + 1) }.sum()

    fun part1(input: List<String>): Long {
        return input
            .map { it.split(" ") }
            .map { (cards, bid) -> Hand(cards, detectType(cards), bid.toLong()) }
            .sortedWith(handComparator(listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')))
            .totalResult()
    }

    fun part2(input: List<String>): Long {
        return input
            .map { it.split(" ") }
            .map { (cards, bid) -> Hand(cards, detectTypeWithJokers(cards), bid.toLong()) }
            .sortedWith(handComparator(listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J')))
            .totalResult()
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 6440L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 5905L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
