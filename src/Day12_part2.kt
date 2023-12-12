fun main() {
    val day = "Day12"

    // MODEL
    data class CondRecord(
        val springs: String,
        val operationalRuns: List<Long>
    )

    // PARSE
    fun String.parseRecord() = split(" ")
        .let { (springs, runs) -> CondRecord(springs, runs.split(",").map { it.toLong() }) }

    // SOLVE
    fun String.branch(): Sequence<String> {
        if ('?' !in this) {
            return sequenceOf(this)
        }
        return sequenceOf(replaceFirst('?', '.'), replaceFirst('?', '#'))
            .flatMap { it.branch() }
    }

    val regex = "\\.+".toRegex()

    fun String.matches(runs: List<Long>) = regex.split(this)
        .map { it.length.toLong() }
        .filter { it > 0 } == runs

    fun CondRecord.resolvedSprings() = springs.branch()
        .filter { it.matches(operationalRuns) }
        .toList()

    fun CondRecord.arrangements(): Long {
        return resolvedSprings().count().toLong()
    }

    fun part1(input: List<String>): Long {
        val records = input.map { it.parseRecord() }
        val resolved = records.map { it.resolvedSprings() }
        resolved.maxBy { it.size }.forEach(::println)
        return resolved.sumOf { it.size }.toLong()
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 21L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 0L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
