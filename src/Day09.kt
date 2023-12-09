// MODEL
typealias NumberSeq = List<Long>

typealias ResolvedNumberSeq = List<NumberSeq>

fun main() {
    val day = "Day09"

    // PARSE
    fun String.parseNumberSeq(): NumberSeq = toLongs()

    // SOLVE
    fun NumberSeq.shouldDoNext() = any { it != 0L }

    fun NumberSeq.next(): NumberSeq = windowed(2) { (a, b) -> b - a }

    fun NumberSeq.resolve(): ResolvedNumberSeq = generateSequence(this) {
        if (it.shouldDoNext()) it.next() else null
    }.toList()

    fun NumberSeq.append(prev: Long): NumberSeq = this + (last() + prev)

    fun NumberSeq.prepend(prev: Long): NumberSeq = listOf(first() - prev) + this

    fun ResolvedNumberSeq.extendFromRight(): ResolvedNumberSeq = reversed()
        .runningFold(last()) { prevSeq, curSeq -> curSeq.append(prevSeq.last()) }
        .reversed()

    fun ResolvedNumberSeq.extendFromLeft(): ResolvedNumberSeq = reversed()
        .runningFold(last()) { prevSeq, curSeq -> curSeq.prepend(prevSeq.first()) }
        .reversed()

    fun ResolvedNumberSeq.resultFromRight(): Long = first().last()

    fun ResolvedNumberSeq.resultFromLeft(): Long = first().first()

    fun part1(input: List<String>): Long {
        return input
            .map { it.parseNumberSeq() }
            .map { it.resolve() }
            .map { it.extendFromRight() }
            .sumOf { it.resultFromRight() }
    }

    fun part2(input: List<String>): Long {
        return input
            .map { it.parseNumberSeq() }
            .map { it.resolve() }
            .map { it.extendFromLeft() }
            .sumOf { it.resultFromLeft() }
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 114L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 2L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
