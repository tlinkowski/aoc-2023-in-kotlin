fun main() {
    val day = "Day01"

    fun part1(input: List<String>): Int {
        fun String.digits() = asSequence()
            .filter(Character::isDigit)
            .map { Character.digit(it, 10) }

        return input.sumOf {
            10 * it.digits().first() + it.digits().last()
        }
    }

    fun part2(input: List<String>): Int {
        val digitMap = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9
        )

        fun firstDigit(string: String, index: Int): Int? {
            if (Character.isDigit(string[index])) {
                return Character.digit(string[index], 10)
            }
            for ((word, digit) in digitMap) {
                if (string.startsWith(word, index)) {
                    return digit
                }
            }
            return null
        }

        fun lastDigit(string: String, index: Int): Int? {
            if (Character.isDigit(string[index])) {
                return Character.digit(string[index], 10)
            }
            for ((word, digit) in digitMap) {
                if (string.startsWith(word, index - word.length + 1)) {
                    return digit
                }
            }
            return null
        }

        fun String.firstDigit() = indices.firstNotNullOf { index -> firstDigit(this, index) }

        fun String.lastDigit() = indices.reversed().firstNotNullOf { index -> lastDigit(this, index) }

        return input.sumOf {
            10 * it.firstDigit() + it.lastDigit()
        }
    }

    // TESTS
    val part1 = part1(readInput("$day/test1"))
    check(part1 == 142) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 281) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
