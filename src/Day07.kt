enum class CardType {
    FIVE {
        override fun detect1(cards: String) = cards.toCharMap().values.any { it == 5 }

        override fun detect2(cards: String) = cards.toCharMap().let { map ->
            map.jokerCount() == 5 || map.withoutJoker()[0].value == 5 - map.jokerCount()
        }
    },
    FOUR {
        override fun detect1(cards: String) = cards.toCharMap().values.any { it == 4 }

        override fun detect2(cards: String) = cards.toCharMap().let { map ->
            map.withoutJoker()[0].value == 4 - map.jokerCount()
        }
    },
    FULL {
        override fun detect1(cards: String) = cards.toCharMap().let { map ->
            map.values.any { it == 3 } && map.values.any { it == 2 }
        }

        override fun detect2(cards: String) = cards.toCharMap().let { map ->
            val withoutJoker = map.withoutJoker()
            when (map.jokerCount()) {
                1 -> withoutJoker[0].value == 3 && withoutJoker[1].value == 1
                        || withoutJoker[0].value == 2 && withoutJoker[1].value == 2

                0 -> withoutJoker[0].value == 3 && withoutJoker[1].value == 2
                else -> false
            }
        }
    },
    THREE {
        override fun detect1(cards: String) = cards.toCharMap().values.any { it == 3 }

        override fun detect2(cards: String) = cards.toCharMap().let { map ->
            map.withoutJoker()[0].value == 3 - map.jokerCount()
        }
    },
    TWO_PAIR {
        override fun detect1(cards: String) = cards.toCharMap().values.count { it == 2 } == 2

        override fun detect2(cards: String) = cards.toCharMap().let { map ->
            val withoutJoker = map.withoutJoker()
            when (map.jokerCount()) {
                1 -> withoutJoker[0].value == 2 && withoutJoker[1].value == 1
                0 -> withoutJoker[0].value == 2 && withoutJoker[1].value == 2
                else -> false
            }
        }
    },
    ONE_PAIR {
        override fun detect1(cards: String) = cards.toCharMap().values.any { it == 2 }

        override fun detect2(cards: String) = cards.toCharMap().let { map ->
            map.withoutJoker()[0].value == 2 - map.jokerCount()
        }
    },
    HIGH_CARD {
        override fun detect1(cards: String) = cards.toCharMap().values.all { it == 1 }

        override fun detect2(cards: String) = detect1(cards)
    };

    abstract fun detect1(cards: String): Boolean

    abstract fun detect2(cards: String): Boolean

    companion object {
        fun String.toCharMap() = toCharArray()
            .groupBy { it }
            .map { (c, list) -> c to list.size }
            .sortedByDescending { (_, size) -> size }
            .toMap()

        fun Map<Char, Int>.withoutJoker() = this.entries.filter { (c, _) -> c != 'J' }

        fun Map<Char, Int>.jokerCount() = getOrDefault('J', 0)
    }
}

fun main() {

    val day = "Day07"

    fun detectType1(cards: String) = CardType.entries.first { it.detect1(cards) }

    fun detectType2(cards: String) = CardType.entries.first { it.detect2(cards) }

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

    fun part1(input: List<String>): Long {
        val handComparator = handComparator(listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2'))
        return input
            .map { it.split(" ") }
            .map { (cards, bid) -> Hand(cards, detectType1(cards), bid.toLong()) }
            .sortedWith(handComparator)
            .mapIndexed { index, hand -> hand.result(index + 1) }
            .sum()
    }

    fun part2(input: List<String>): Long {
        val handComparator = handComparator(listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J'))
        return input
            .map { it.split(" ") }
            .map { (cards, bid) -> Hand(cards, detectType2(cards), bid.toLong()) }
            .sortedWith(handComparator)
            .mapIndexed { index, hand -> hand.result(index + 1) }
            .sum()
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
