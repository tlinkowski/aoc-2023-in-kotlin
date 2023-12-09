fun main() {
    val day = "Day09"

    // MODEL
    data class NumberSeq(val numbers: List<Long>)

    data class ResolvedNumberSeq(val seqs: List<NumberSeq>)

    // PARSE
    fun String.parseNumberSeq() = NumberSeq(toLongs())

    // SOLVE
    fun NumberSeq.shouldDoNext() = !numbers.all { it == 0L }

    fun NumberSeq.next() = NumberSeq(
        numbers.windowed(2) { (a, b) -> b - a }
    )

    fun NumberSeq.resolve() = ResolvedNumberSeq(
        generateSequence(this) { if (it.shouldDoNext()) it.next() else null }.toList()
    )

    fun NumberSeq.append(prev: Long) = NumberSeq(numbers + listOf(numbers.last() + prev))

    fun ResolvedNumberSeq.extend(): ResolvedNumberSeq {
        val newSeqs = ArrayList<NumberSeq>(seqs)
        var last = 0L
        for (i in seqs.indices.reversed()) {
            newSeqs[i] = newSeqs[i].append(last)
            last = newSeqs[i].numbers.last()
        }
//        seqs.reversed().forEach {
//            newSeqs.add(it.append(last))
//            last = it.numbers.last()
//        }
        return ResolvedNumberSeq(newSeqs)//.reversed())
    }

    fun ResolvedNumberSeq.extend2() = ResolvedNumberSeq(
        seqs.reversed()
            .drop(1)
            .runningFold(seqs.first().append(0L)) { a, b -> a.append(b.numbers.last()) }
            .reversed()
    )

    fun ResolvedNumberSeq.result() = seqs.first().numbers.last()

    fun part1(input: List<String>): Long {
        val b = input
            .map { it.parseNumberSeq() }
            .map { it.resolve() }
        println(b)
        val c = b
            .map { it.extend() }
        println(c)
        return c
            .sumOf { it.result() }
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 114L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 0L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
