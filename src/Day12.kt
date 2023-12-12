fun main() {
    val day = "Day12"

    // MODEL
    data class Row(val springs: String, val hashRuns: List<Int>) {
        val totalHashCount = hashRuns.sum()
        val maxDotCount = springs.length - totalHashCount
    }

    // assumption: every row starts and ends with a dot run (even with a length of 0),
    // so it's "dot run - hash run - dot run - hash run - ... - dot run"
    data class Match(
        val dotRunSize: Int, // equivalent of ["", "...", ".."].size = 3
        val totalDotCount: Int // equivalent of ["", "...", ".."].sumOf { it.length } = 5
    )

    // PARSE
    fun String.parseRow(): Row = split(" ").let { (springs, hashRunString) ->
        Row(springs, hashRunString.split(",").map { it.toInt() })
    }

    // SOLVE
    fun Match.totalMatchedHashCount(row: Row) = (0..<dotRunSize).sumOf { i -> row.hashRuns[i] }

    fun Row.matchesNextDotsAndHashes(match: Match, nextDotCount: Int): Boolean {
        val iSpring = match.totalDotCount + match.totalMatchedHashCount(this)

        val iLastDot = iSpring + nextDotCount
        val potentialDotRange = iSpring..<iLastDot
        if (potentialDotRange.any { i -> springs[i] == '#' }) {
            return false
        }

        val nextHashCount = hashRuns.getOrNull(match.dotRunSize)
        if (nextHashCount != null) {
            val iLastHash = iLastDot + nextHashCount
            val potentialHashRange = iLastDot..<iLastHash
            if (potentialHashRange.any { i -> springs[i] == '.' } || springs.getOrNull(iLastHash) == '#') {
                return false
            }
        }

        return true
    }

    operator fun Match.plus(nextDotCount: Int) = Match(
        dotRunSize = dotRunSize + 1,
        totalDotCount = totalDotCount + nextDotCount
    )

    fun Row.countMatchingDotRunsCached(match: Match, resultCache: MutableMap<Match, Long>): Long {
        var result = resultCache[match]
        if (result != null) {
            return result
        }

        fun Row.countMatchingDotRuns(): Long {
            val maxNextDotCount = maxDotCount - match.totalDotCount
            if (match.dotRunSize == hashRuns.size) { // only final dot run left to fill
                return if (matchesNextDotsAndHashes(match, maxNextDotCount)) 1 else 0
            }

            val minNextDotCount = if (match.dotRunSize == 0) 0 else 1
            return (minNextDotCount..maxNextDotCount)
                .filter { nextDotCount -> matchesNextDotsAndHashes(match, nextDotCount) }
                .sumOf { nextDotCount -> countMatchingDotRunsCached(match + nextDotCount, resultCache) }
        }

        result = countMatchingDotRuns()
        resultCache[match] = result
        return result
    }

    fun Row.arrangementCount(): Long = countMatchingDotRunsCached(Match(0, 0), HashMap())

    fun Row.unfold(times: Int) = Row(
        springs = generateSequence { springs }.take(times).joinToString("?"),
        hashRuns = generateSequence { hashRuns }.take(times).flatten().toList()
    )

    fun part1(input: List<String>): Long {
        val rows = input.map { it.parseRow() }
        return rows.sumOf { it.arrangementCount() }
    }

    fun part2(input: List<String>): Long {
        val rows = input.map { it.parseRow().unfold(times = 5) }
        return rows.sumOf { it.arrangementCount() }
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 21L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 525152L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    val realPart1 = part1(input)
    println("Part 1: $realPart1")
    check(realPart1 == 7916L) { realPart1 }
    println("Part 2: " + part2(input))
}
